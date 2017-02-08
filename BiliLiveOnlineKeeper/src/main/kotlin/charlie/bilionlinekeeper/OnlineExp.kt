package charlie.bilionlinekeeper

import charlie.bililivelib.exceptions.BiliLiveException
import charlie.bililivelib.user.HeartbeatProtocol
import charlie.bililivelib.user.Session
import charlie.bilionlinekeeper.util.I18n
import charlie.bilionlinekeeper.util.LogUtil
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import java.util.*

class OnlineExp(session: Session) {
    private fun logSingleException(ex: BiliLiveException) {
        LogUtil.logException(
                logger,
                Level.WARN,
                I18n.getString("onlineExp.exception")!!,
                ex)
    }

    private val PERIOD_MILLIS = (5 * 1000L)

    private val protocol = HeartbeatProtocol(session)
    private val timer = Timer("OnlineExp", false)
    private val logger = LogManager.getLogger("onlineExp")
    private var task: TimerTask? = null
    private var started: Boolean = false

    fun start() {
        started = true
        task = OnlineExpTask().apply {
            timer.schedule(this, 0L, PERIOD_MILLIS)
        }
        logStart()
    }

    private fun logStart() {
        logger.info(I18n.getString("onlineExp.start"))
    }

    fun stop() {
        (task ?: return).cancel()
        started = false
        logStop()
    }

    private fun logStop() {
        logger.info(I18n.getString("onlineExp.stop"))
    }

    fun isStarted(): Boolean = started

    private inner class OnlineExpTask : TimerTask() {
        override fun run() {
            try {
                protocol.heartbeat()
            } catch (ex: BiliLiveException) {
                logSingleException(ex)
            }
        }
    }
}