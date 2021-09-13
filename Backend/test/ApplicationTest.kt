package hu.bme.aut.onlab.poker

import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
                assertNull(response.content)
            }
        }
    }
}
