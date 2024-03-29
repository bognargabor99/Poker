package hu.bme.aut.thesis.poker.integrationtest

import com.google.gson.Gson
import hu.bme.aut.thesis.poker.data.DatabaseHelper
import hu.bme.aut.thesis.poker.data.UserAuthInfo
import hu.bme.aut.thesis.poker.module
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.*
import kotlin.test.*

@DelicateCoroutinesApi
class ApplicationTest {
    @Test
    fun testRootOnHttp() {
        withTestApplication(Application::module) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.MovedPermanently, response.status())
            }
            this.stop(1000, 10000)
        }
    }

    @Test
    fun testRedirectHttps() {
        withTestApplication {
            application.install(XForwardedHeaderSupport)
            application.module()
            handleRequest(HttpMethod.Get, "/") {
                addHeader(HttpHeaders.XForwardedProto, "https")
            }.let { call ->
                assertEquals(HttpStatusCode.OK, call.response.status())
                assertEquals("it works!", call.response.content)
            }
            this.stop(1000, 10000)
        }
    }

    @Test
    fun testAuthentication() {
        withTestApplication {
            application.install(XForwardedHeaderSupport)
            application.module()
            handleRequest(HttpMethod.Get, "/authenticate") {
                addHeader(HttpHeaders.XForwardedProto, "https")
                val base64Credentials = String(Base64.getEncoder().encode("admin:admin".toByteArray()))
                addHeader(HttpHeaders.Authorization, "Basic $base64Credentials")
            }.let { call ->
                assertEquals(HttpStatusCode.OK, call.response.status())
                assertEquals("Hello, admin!", call.response.content)
            }
            this.stop(1000, 10000)
        }
    }

    @Test
    fun testRegistrationFailure() {
        withTestApplication {
            application.install(XForwardedHeaderSupport)
            application.module()
            handleRequest(HttpMethod.Post, "/register") {
                addHeader(HttpHeaders.XForwardedProto, "https")
                addHeader(HttpHeaders.ContentType, "application/json")
                val body = Gson().toJson(UserAuthInfo("admin", "admin"))
                setBody(body)
            }.let { call ->
                assertEquals(HttpStatusCode.Conflict, call.response.status())
                assertEquals("Username already in use: admin", call.response.content)
            }
            this.stop(1000, 10000)
        }
    }

    @Test
    fun testRegistrationSuccess() {
        withTestApplication {
            application.install(XForwardedHeaderSupport)
            application.module()
            handleRequest(HttpMethod.Post, "/register") {
                addHeader(HttpHeaders.XForwardedProto, "https")
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(Gson().toJson(UserAuthInfo("localadmin", "localadmin")))
            }.let { call ->
                assertEquals(HttpStatusCode.Created, call.response.status())
                assertEquals("Registered new user: localadmin", call.response.content)
            }
            DatabaseHelper.deleteUser("localadmin")
            this.stop(1000, 10000)
        }
    }
}
