package org.lappsgrid.health.service

import groovy.util.logging.Slf4j
import org.lappsgrid.health.model.LappsgridService
import org.lappsgrid.health.model.Services
import org.lappsgrid.health.util.Settings
import org.lappsgrid.health.util.Status
import org.lappsgrid.health.util.Time
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import redis.clients.jedis.Jedis

import java.time.ZonedDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 *
 */
@Service
@Slf4j("logger")
class StatusService {
//    enum Status { UP, DOWN }

    static final int ONE_HOUR = 3600000

    @Autowired
    private MailService mail

//    @Autowired
    private ThreadService threads

//    @Autowired
    private UpdateService updater

    protected Services vassarServices
    protected Services brandeisServices
//    private ScheduledExecutorService executor
    Map<String,Integer> table

    ZonedDateTime lastUpdate
    ZonedDateTime lastHealthCheck
    int up
    int down

    StatusService(MailService mail, UpdateService updater, ThreadService threads) {
        this.mail = mail
        this.updater = updater
        this.threads = threads

//        executor = Executors.newScheduledThreadPool(1)
//        updater = new UpdateService()
        vassarServices = updater.update("vassar")
        brandeisServices = updater.update("brandeis")
        lastUpdate = Time.now()
        table = [:]
        init()

        logger.info("Scheduling the ServiceUpdate task to run every 24 hours")
        threads.scheduleAtFixedRate(new ServiceUpdate(), 24, 24, TimeUnit.HOURS)
    }

    private void init() {
        logger.info("Initializing the StatusService")
        Jedis redis = new Jedis(Settings.REDIS_HOST)
        down = up = 0
        vassarServices.services.each { s ->
            String status = redis.get(s.id) ?: Status.ERROR
            if (status == Status.OK) {
                ++up
            }
            else {
                ++down
            }
            table[s.id] = status
        }
        brandeisServices.services.each { s ->
            String status = redis.get(s.id) ?: Status.ERROR
            if (status == Status.OK) {
                ++up
            }
            else {
                ++down
            }
            table[s.id] = status
        }
        String t = redis.get(Settings.LAST_CHECK_KEY)
        ZonedDateTime last = ZonedDateTime.parse(t)
        ZonedDateTime now = Time.now()
        long msec = Time.between(last, now)
        if (msec > ONE_HOUR) {
            logger.info("Doing an immediate health check.")
            lastHealthCheck = Time.epoch()
            threads.scheduleAtFixedRate(new HealthCheck(), 1, 1, TimeUnit.HOURS)
            threads.schedule(new HealthCheck(false), 0, TimeUnit.MILLISECONDS)
        }
        else {
            int minutes = msec / 60000
            int delay = 60 - minutes
            logger.info("The next health check will be in {} minutes", minutes)
            lastHealthCheck = last
            threads.scheduleAtFixedRate(new HealthCheck(), delay, 60, TimeUnit.MINUTES)
        }
        //lastHealthCheck = Time.now()
    }

    void update() {
        new ServiceUpdate().run()
    }

    void check() {
        new HealthCheck().run()
    }

    /*
     * Polls the two ServiceManager instances for the list of available services.
     */
    @Slf4j("logger")
    class ServiceUpdate implements Runnable {

        void run() {
            logger.info("Updating Vassar services")
            vassarServices = updater.update("vassar")
            logger.info("Updating Brandeis services")
            brandeisServices = updater.update("brandeis")
            lastUpdate = Time.now()
        }
    }

    @Slf4j("logger")
    class HealthCheck implements Runnable {
        boolean sendmail

        HealthCheck(boolean sendmail=true) {
            this.sendmail = sendmail
        }

        void run() {
            int errors = 0
            up = down = 0
            try {
                check(vassarServices)
            }
            catch(Exception e) {
                ++errors
                logger.error("Error checking Vassar services", e)
            }
            try {
                check(brandeisServices)
            }
            catch (Exception e) {
                ++errors
                logger.error("Error checking Brandeis services", e)
            }
//            if (errors == 0) {
                lastHealthCheck = Time.now()
                new Jedis(Settings.REDIS_HOST).set(Settings.LAST_CHECK_KEY, lastHealthCheck.toString())
                logger.info("Health check completed at {}", lastHealthCheck)
//            }
        }

        void check(Services services) {
            Jedis redis = new Jedis(Settings.REDIS_HOST)
            logger.info("Checking services for {}", services.url)
//            up = down = 0
            List brandeisUp = []
            List brandeisDown = []
            List vassarUp = []
            List vassarDown = []
            services.services.each { svc ->
                String previousStatus = redis.get(svc.id)
                if (previousStatus == null) previousStatus = Status.ERROR
                String status = svc.update()
                if (status != Status.OK) {
                    logger.debug("Retrying service {}", svc.id)
                    sleep(500)
                    status = svc.update()
                }
                redis.set(svc.id, status)
                if (status == Status.OK) {
                    ++up
                    if (sendmail && previousStatus != Status.OK) {
                        logger.info("{} has returned to service", svc.id)
                        if (svc.id.startsWith("anc:")) {
                            vassarUp.add(svc)
                        }
                        else {
                            brandeisUp.add(svc)
                        }
                    }
                    table[svc.id] = Status.OK
                }
                else {
                    logger.info("{} is offline. Status: {}", svc.id, status)
                    ++down
                    if (sendmail && previousStatus == Status.OK) {
                        // The service has gone down.
                        if (svc.id.startsWith("anc:")) {
                            vassarDown.add(svc)
                        }
                        else {
                            brandeisDown.add(svc)
                        }
                    }
                    table[svc.id] = status
                }
            }

            if (vassarUp.size() > 0 || vassarDown.size() > 0) {
                logger.info("Sending mail for Vassar services")
                StringBuilder buffer = new StringBuilder()
                if (vassarUp.size() > 0) {
                    buffer.append("The following services have come back online:\n\n")
                    buffer.append(format(vassarUp))
                    buffer.append("\n")
                }
                if (vassarDown.size() > 0) {
                    buffer.append("The following services have gone down:\n\n")
                    buffer.append(format(vassarDown))
                    buffer.append("\n")
                }
                threads.schedule(new Mailer("suderman@cs.vassar.edu", buffer.toString()))
            }
            if (brandeisUp.size() > 0 || brandeisDown.size() > 0) {
                logger.info("Sending mail for Brandeis services")
                StringBuilder buffer = new StringBuilder()
                if (brandeisUp.size() > 0) {
                    buffer.append("The following services have come back online:\n\n")
                    buffer.append(format(brandeisUp))
                    buffer.append("\n")
                }
                if (brandeisDown.size() > 0) {
                    buffer.append("The following services have gone down:\n\n")
                    buffer.append(format(brandeisDown))
                    buffer.append("\n")
                }
                threads.schedule(new Mailer("marc@cs.brandeis.edu, krim@brandeis.edu", buffer.toString()))
            }
        }

        String format(List<LappsgridService> services) {
            return services.collect { s -> "\t- ${s.id}\t${s.name}"}.join("\n")
        }
    }

    class Mailer implements Runnable {
        String to
        String message

        Mailer(String to, String message) {
            this.to = to
            this.message = message
        }

        void run() {
            mail.send(to, message)
        }
    }
}
