package org.lappsgrid.health.controllers

import groovy.util.logging.Slf4j
import org.lappsgrid.health.service.StatusService
import org.lappsgrid.health.service.ThreadService
import org.lappsgrid.health.util.HTML
import org.lappsgrid.health.util.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 */
@RestController
@Slf4j("logger")
class StatusController {

    StatusService status

    ThreadService threads

    @Autowired
    StatusController(StatusService status, ThreadService threads) {
        this.status = status
        this.threads = threads
    }

    @GetMapping(path="/", produces = "text/html")
    ResponseEntity<String> htmlStatus() {
        String html = HTML.render('Service Status') {
            h1 'Service Status'
            h2 {
                span 'Last updated on '
                span class:'date', status.lastHealthCheck
            }
            table {
                tr {
                    th "Service ID"
                    th "Status"
                }
                status.table.sort().each { id, s ->
                    tr {
                        td id
                        if (s == Status.OK) {
                            td(class:'up', 'UP')
                        }
                        else if (s == Status.ERROR) {
                            td(class:'down', 'DOWN')
                        }
                        else if (s == Status.NOT_META) {
                            td(class:'broken', 'NO META')
                        }
                        else if (s == Status.PARSE) {
                            td(class:'broken', 'PARSE')
                        }
                        else {
                            td(class:'broken', "Unknown ($s)")
                        }
                    }
                }
            }
        }
        return ResponseEntity.ok(html)
    }

    @GetMapping(path="/status", produces = "text/html")
    ResponseEntity<String> systemStatus() {
        String html = HTML.render('System Status') {
            h1 'System Status'
            table {
                tr {
                    td 'Updated'
                    td class:'date', status.lastUpdate
                }
                tr {
                    td 'Checked'
                    td class:'date', status.lastHealthCheck
                }
                tr {
                    td 'Up'
                    td status.up
                }
                tr {
                    td 'Down'
                    td status.down
                }
            }
        }
        return ResponseEntity.ok(html)
    }

    @GetMapping(path="/check", produces = "text/html")
    ResponseEntity<String> check() {
        Runnable task = {
            status.check()
        }
        threads.schedule(task)
        String html = HTML.render {
            h1 'Status Check'
            p 'The service check will take a minute or two.'
        }
        return ResponseEntity.ok(html)
    }

    @GetMapping(path="/update", produces = "text/html")
    ResponseEntity<String> update() {
        Runnable task = {
            status.update()
            status.check()
        }
        threads.schedule(task)
        String html = HTML.render {
            h1 'Status Update'
            p "The list of available services will be updated and then each service will be pinged to determine its online status."
        }
        return ResponseEntity.ok(html)
    }
}
