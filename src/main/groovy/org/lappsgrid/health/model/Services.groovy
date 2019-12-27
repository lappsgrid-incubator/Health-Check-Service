package org.lappsgrid.health.model

import org.lappsgrid.health.util.Time

import java.time.ZonedDateTime

/**
 *
 */
class Services {
    String url
    int totalCount
    List<LappsgridService> services
    ZonedDateTime timestamp

    Services() {
        timestamp = Time.now()
    }

    int size() { return services.size() }
}


// 973-849-1061