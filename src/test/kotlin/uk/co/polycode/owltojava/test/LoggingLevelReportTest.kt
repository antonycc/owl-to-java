package uk.co.polycode.owltojava.test

import mu.KotlinLogging
import org.slf4j.impl.StaticLoggerBinder
import kotlin.test.Test

private val logger = KotlinLogging.logger {}

internal class LoggingLevelReportTest {

    @Test
    fun testLogger() {
        logger.trace("Test logged using SLF4J API at level: Trace")
        logger.debug("Test logged using SLF4J API at level: Debug")
        logger.info("Test logged using SLF4J API at level: Info")
        logger.warn("The current logging implementation is ${StaticLoggerBinder.getSingleton().loggerFactory}")
        logger.warn("Test logged using SLF4J API at level: Error")
        logger.error("Test logged using SLF4J API at level: Warning")
    }
}
