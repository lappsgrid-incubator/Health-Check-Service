package org.lappsgrid.health.model

import org.junit.Test

/**
 *
 */
class LappsgridServiceTest {

    @Test
    void getServiceIdFromId() {
        LappsgridService service = new LappsgridService()
        service.id = "foo.bar_1.2.3"

        assert "foo.bar" == service.serviceId
    }

    @Test
    void getVersionFromId() {
        LappsgridService service = new LappsgridService()
        service.id = "foo.bar_1.2.3"

        Version expected = new Version("1.2.3")
        assert expected.equals(service.version)

    }

}
