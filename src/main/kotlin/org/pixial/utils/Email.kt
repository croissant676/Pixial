package org.pixial.utils

import io.ktor.server.application.*
import kotlinx.coroutines.launch
import net.axay.simplekotlinmail.delivery.MailerManager
import net.axay.simplekotlinmail.delivery.mailerBuilder
import net.axay.simplekotlinmail.delivery.send
import net.axay.simplekotlinmail.email.emailBuilder

lateinit var senderEmail: String
    private set

fun Application.createMailer() {
    val host = config["pixial.email.host"]?.stringValueOrNull ?: "localhost"
    val port = config["pixial.email.port"]?.stringValueOrNull?.toIntOrNull() ?: 25
    val username = config["pixial.email.username"]?.stringValueOrNull
    senderEmail = username ?: throw IllegalArgumentException("Must specify a username in application.conf: Property \"pixial.email.username\"")
    val password = config["pixial.email.password"]?.stringValueOrNull
    val mailConfigValue = config.config("pixial.email.props")
    MailerManager.defaultMailer = mailerBuilder(
        host, port, username, password
    ) {
        properties["mail.smtp.host"] = host
        properties["mail.smtp.port"] =  port.toString()
        val keys = mailConfigValue.keys()
        val debugMap = mutableMapOf<String, String?>()
        for(key in keys) {
            val value = mailConfigValue[key]?.stringValueOrNull
            properties["mail.$key"] = value
            debugMap[key] = value
        }
        kLogger.debug { "Mailer: visited keys $keys: $debugMap." }
    }
}

