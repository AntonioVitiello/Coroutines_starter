package it.av.coroutines_core

import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Antonio Vitiello on 27/02/2023.
 */
class Log {
    companion object {
        private val shortTimeFormat by lazy { SimpleDateFormat("HH:mm:ss.SSS", Locale.ITALY) }

        fun v(tag: String, msg: String) {
            log("$tag: $msg", LogLevel.VERBOSE)
        }

        fun d(tag: String, msg: String) {
            log("$tag: $msg", LogLevel.DEBUG)
        }

        fun i(tag: String, msg: String) {
            log("$tag: $msg", LogLevel.INFO)
        }

        fun w(tag: String, msg: String) {
            log("$tag: $msg", LogLevel.WARNING)
        }

        fun e(tag: String, thr: Throwable) {
            e(tag, "", thr)
        }

        fun e(tag: String, msg: String, thr: Throwable? = null) {
            val stackTrace = if (thr != null) {
                val stringWriter = StringWriter()
                thr.printStackTrace(PrintWriter(stringWriter))
                stringWriter.toString()
            } else ""
            log("$tag: $msg, $stackTrace", LogLevel.ERROR)
        }

        private fun log(msg: String, logLevel: LogLevel = LogLevel.EMPTY) {
            val time = shortTimeFormat.format(Date())
            println("$time ${logLevel.initial} [${Thread.currentThread().name}] $msg")
        }
    }

    enum class LogLevel(val initial: String) {
        VERBOSE("V"),
        DEBUG("D"),
        INFO("I"),
        WARNING("W"),
        ERROR("E"),
        EMPTY("")
    }

}