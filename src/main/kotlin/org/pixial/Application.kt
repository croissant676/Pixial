package org.pixial

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.pixial.utils.*
import org.slf4j.event.Level

lateinit var application: Application
    private set

val developmentLogger = KotlinLogging.logger { }

fun main(args: Array<String> = emptyArray()) = EngineMain.main(args)

fun Application.module() {
    application = this
    installPlugins()
    installStatusPages()
    checkHoconDeletionStatus()
    createMailer()
}

fun Application.installPlugins() {
    install(IgnoreTrailingSlash)
    install(AutoHeadResponse)
    install(DoubleReceive)
    install(Compression) {
        gzip()
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
        })
    }
    install(CallLogging) {
        logger = KotlinLogging.logger("CallLogging")
        level = Level.DEBUG
        val port = this@installPlugins.environment.config.port
        format {
            val request = it.request
            val response = it.response
            "${request.httpMethod.value}: http://localhost:${port}${request.uri} : ${response.status()}"
        }
    }
    install(CallId) {
        generate { base64Encoder.encodeToString(random.nextBytes(16)) }
    }
}

fun Application.onStartup() {

}