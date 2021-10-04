package hu.bme.aut.onlab.poker

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlin.test.*
import io.ktor.server.testing.*
import hu.bme.aut.onlab.poker.plugins.configureRouting

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Get, "/register").apply {
                assertEquals(HttpStatusCode.MovedPermanently, response.status())
            }
        }
    }

    @Test
    fun testRedirectHttps() {
        withTestApplication {
            application.install(XForwardedHeaderSupport)
            application.module(testing = true)
            handleRequest(HttpMethod.Get, "/register") {
                addHeader(HttpHeaders.XForwardedProto, "https")
            }.let { call ->
                assertEquals(HttpStatusCode.Created, call.response.status())
                assertEquals("itt a srác", call.response.content)
            }
        }
    }

}
