package hu.bme.aut.onlab.poker

import com.google.gson.Gson
import hu.bme.aut.onlab.poker.data.DatabaseHelper
import hu.bme.aut.onlab.poker.data.UserAuthInfo
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
        }
    }

    @Test
    fun testRootOnHttps() {
        withTestApplication {
            application.install(XForwardedHeaderSupport)
            application.module(true)
            handleRequest(HttpMethod.Get, "/")  {
                addHeader(HttpHeaders.XForwardedProto, "https")
            }.let { call ->
                assertEquals(HttpStatusCode.OK, call.response.status())
                assertEquals("it works!", call.response.content)
            }
        }
    }

    @Test
    fun testRedirectHttps() {
        withTestApplication {
            application.install(XForwardedHeaderSupport)
            application.module(testing = true)
            handleRequest(HttpMethod.Get, "/") {
                addHeader(HttpHeaders.XForwardedProto, "https")
            }.let { call ->
                assertEquals(HttpStatusCode.OK, call.response.status())
                assertEquals("it works!", call.response.content)
            }
        }
    }

    @Test
    fun testAuthentication() {
        withTestApplication {
            application.install(XForwardedHeaderSupport)
            application.module(testing = true)
            handleRequest(HttpMethod.Get, "/authenticate") {
                addHeader(HttpHeaders.XForwardedProto, "https")
                val plainCredentials = "admin:admin"
                val base64Credentials= String(Base64.getEncoder().encode(plainCredentials.toByteArray()))
                addHeader(HttpHeaders.Authorization, "Basic $base64Credentials")
            }.let { call ->
                assertEquals(HttpStatusCode.OK, call.response.status())
                assertEquals("Hello, admin!", call.response.content)
            }
        }
    }

    @Test
    fun testRegistrationFailure() {
        withTestApplication {
            application.install(XForwardedHeaderSupport)
            application.module(testing = true)
            handleRequest(HttpMethod.Post, "/register") {
                addHeader(HttpHeaders.XForwardedProto, "https")
                addHeader(HttpHeaders.ContentType, "application/json")
                val body = Gson().toJson(UserAuthInfo("admin", "admin"))
                setBody(body)
            }.let { call ->
                assertEquals(HttpStatusCode.Conflict, call.response.status())
                assertEquals("Username already in use: admin", call.response.content)
            }
        }
    }

    @Test
    fun testRegistrationSuccess() {
        withTestApplication {
            DatabaseHelper.deleteUser("localadmin")
            application.install(XForwardedHeaderSupport)
            application.module(testing = true)
            handleRequest(HttpMethod.Post, "/register") {
                addHeader(HttpHeaders.XForwardedProto, "https")
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody(Gson().toJson(UserAuthInfo("localadmin", "localadmin")))
            }.let { call ->
                assertEquals(HttpStatusCode.Created, call.response.status())
                assertEquals("Registered new user: localadmin", call.response.content)
            }
        }
    }
}
