package org.lappsgrid.health.service

import groovy.util.logging.Slf4j
import groovyx.net.http.FromServer
import groovyx.net.http.HttpBuilder
import org.lappsgrid.health.json.Serializer
import org.lappsgrid.health.model.LappsgridService
import org.lappsgrid.health.model.Services
import org.springframework.stereotype.Service

import static groovyx.net.http.HttpBuilder.configure;

/**
 *
 */
@Service
@Slf4j("logger")
class UpdateService {
    static final String host = "https://api.lappsgrid.org"

    Services update(String org) {
        HttpBuilder http = configure {
            request.uri = host
        }
        boolean save = false
        Services info = new Services()
        http.get {
            request.uri.path = '/services/' + org
            request.accept = "application/json"
            response.success { FromServer resp, Object body ->
                save = true
                info.url = body.url
                info.totalCount = body.totalCount
                info.services = []
                body.elements.each { e ->
                    if (e.serviceType == 'SERVICE.TYPE.PROCESSOR') {
                        LappsgridService service = new LappsgridService(e.serviceId, e.serviceName, e.endpointUrl)
                        info.services.add(service)
                    }
                    else {
                        logger.info("{} is not a processing service", e.serviceId)
                    }
                }
            }
            response.failure { FromServer resp, Object body ->
                if (resp.statusCode == 404) {
                    logger.error("URL was not found on the server.")
                }
                else {
                    logger.error("Unhandled error: {} - {}", resp.statusCode, resp.message)
                }
            }
        }
        return info
    }
}
