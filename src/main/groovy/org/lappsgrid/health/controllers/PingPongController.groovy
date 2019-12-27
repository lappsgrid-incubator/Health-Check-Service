package org.lappsgrid.health.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 *
 */
@RestController
class PingPongController {

    @GetMapping(path="/ping", produces = "text/plain")
    ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong")
    }
}
