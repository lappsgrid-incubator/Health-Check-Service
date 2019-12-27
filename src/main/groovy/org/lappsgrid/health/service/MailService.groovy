package org.lappsgrid.health.service

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service

import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.PasswordAuthentication

import static javax.mail.Session.getInstance

//import javax.mail.*
//import javax.mail.internet.*

/**
 *
 */
@Service
@Slf4j("logger")
class MailService {

    private String user
    private String pass
    private String host
    private boolean disabled

    MailService() {
        Properties props = new Properties()
        File file = new File("/etc/lapps/health.ini")
        if (!file.exists()) {
            file = new File("/run/secrets/health.ini")
            if (!file.exists()) {
                disabled = true
                logger.warn("Sending email has been disabled due to missing configuration information.")
                return
            }
        }
        disabled = false
        props.load(new FileReader(file))
        user = props.get("SMTP_USER", "user")
        pass = props.get("SMTP_PASS", "password")
        host = props.get("SMTP_HOST", "smtp.gmail.com")
        send("suderman@cs.vassar.edu", "The Health Check service is now online.")
        logger.info("Configured the email service.")
    }

    void send(String recipients, String body) {
        if (disabled) {
            logger.warn("Unable to send email to {}", recipients)
            return
        }
        logger.info("Sending mail to: {}", recipients)
        Properties prop = new Properties();
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

//        props.put("mail.smtp.host", "smtp.gmail.com");
//        props.put("mail.smtp.port", "465");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.socketFactory.port", "465");
//        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        Session session = getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, pass);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("health@lappsgrid.org"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipients)
            );
            message.setRecipients(Message.RecipientType.CC,
                new InternetAddress("suderman@cs.vassar.edu"))
            message.setSubject("LAPPS Health Check");
            message.setText(body);

            Transport.send(message);


        } catch (MessagingException e) {
            logger.error("Unable to send mail.", e)
        }
    }
}

