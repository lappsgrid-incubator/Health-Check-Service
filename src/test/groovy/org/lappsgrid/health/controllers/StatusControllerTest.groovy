package org.lappsgrid.health.controllers

import org.junit.Test
import org.junit.runner.RunWith
import org.lappsgrid.health.service.StatusService
import org.lappsgrid.health.service.ThreadService
import org.lappsgrid.health.util.Status
import org.lappsgrid.health.util.Time
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import java.time.ZonedDateTime

/**
 *
 */
@RunWith(SpringRunner)
@WebMvcTest(controllers = StatusController)
class StatusControllerTest {
    @Autowired
    MockMvc mvc

    @MockBean
    StatusService status
    @MockBean
    ThreadService threads
    @Test
    void getHtmlStatus() {
        Map<String,String> table = [
                ok: Status.OK,
                error: Status.ERROR,
                no_data: Status.NO_DATA,
                not_meta: Status.NOT_META

        ]
        ZonedDateTime now = Time.now()
        Mockito.when(status.getTable()).thenReturn(table)
        Mockito.when(status.getLastHealthCheck()).thenReturn(now)
        Mockito.when(status.getLastUpdate()).thenReturn(now)
        def response = mvc.perform(get("/status"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
        String html = response.contentAsString
        println html
        def document = new XmlParser().parseText(html)
        assert 2 == document.depthFirst().findAll{ it.'@class' == 'date'}.size()
    }

    @Test
    void getSystemStatus() {
        ZonedDateTime now = Time.now()
        Mockito.when(status.getLastHealthCheck()).thenReturn(now)
        Mockito.when(status.getLastUpdate()).thenReturn(now)
        def response = mvc.perform(get("/status"))
            .andReturn()
            .getResponse()
        assert HttpStatus.OK.value() == response.status
        String body = response.contentAsString
        println body
        Node html = new XmlParser().parseText(body)
        assert 4 == html.body.div[1].table.tr.size()
    }
}
