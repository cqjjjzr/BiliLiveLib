package charlie.bilionlinekeeper

import charlie.bililivelib.exceptions.BiliLiveException
import charlie.bililivelib.freesilver.FreeSilverProtocol
import charlie.bililivelib.internalutil.MiscUtil
import charlie.bililivelib.user.Session
import charlie.bilionlinekeeper.util.I18n
import charlie.bilionlinekeeper.util.LogUtil
import charlie.bilionlinekeeper.util.TimeUtil
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class FreeSilver(session: Session) {
    private val LOGGER_NAME = "freeSilver"
    private val THREAD_NAME = "FreeSilverGetter"
    private val EXCEPTION_WAIT_TIME_MILLIS: Long = 10 * 60 * 1000 // 10 Minutes

    private val protocol: FreeSilverProtocol = FreeSilverProtocol(session)
    private val logger: Logger = LogManager.getLogger(LOGGER_NAME)
    private var thread: Thread = Thread()

    fun start() {
        thread = Thread(FreeSilverRunnable())
        thread.start()
        logger.info(I18n.getString("freeSilver.start"))
    }

    fun stop() {
        thread.interrupt()
        logger.info(I18n.getString("freeSilver.stop"))
    }

    fun isStarted(): Boolean = thread.isAlive

    private inner class FreeSilverRunnable : Runnable {
        override fun run() {
            initThreadName()
            out@ while (!Thread.currentThread().isInterrupted) {
                try {
                    val currentInfo: FreeSilverProtocol.CurrentSilverTaskInfo = protocol.currentFreeSilverStatus
                    if (!currentInfo.hasRemaining()) logAndStopToday()
                    logCurrentTask(currentInfo)
                    val gotInfo: FreeSilverProtocol.GetSilverInfo
                    try {
                        gotInfo = protocol.waitToGetSilver(currentInfo)
                    } catch(e: InterruptedException) {
                        break@out
                    }

                    when (gotInfo.status()) {
                        FreeSilverProtocol.GetSilverInfo.Status.NOT_LOGGED_IN -> {
                            logNotLoggedIn()
                            break@out
                        }
                        else -> logRound(gotInfo)
                    }
                    if (gotInfo.isEnd) logAndStopToday()
                } catch(e: BiliLiveException) {
                    logRoundException(e)
                    MiscUtil.sleepMillis(EXCEPTION_WAIT_TIME_MILLIS)
                    continue
                }
            }
        }

        private fun logCurrentTask(currentInfo: FreeSilverProtocol.CurrentSilverTaskInfo) {
            logger.info(I18n.format("freeSilver.currentTask",
                    currentInfo.data.minute,
                    currentInfo.data.silverCount))
        }

        private fun logNotLoggedIn() {
            logger.error(I18n.getString("user.logNotLoggedIn"))
        }

        private fun logRound(gotInfo: FreeSilverProtocol.GetSilverInfo) {
            logger.info(I18n.format("freeSilver.gotRound",
                    gotInfo.data.awardSilverCount))
        }

        private fun logRoundException(e: BiliLiveException) {
            LogUtil.logException(logger, Level.WARN, I18n.getString("freeSilver.roundException")!!, e)
        }

        private fun logAndStopToday() {
            logger.info(I18n.getString("freeSilver.stopToday"))
            waitToTomorrow()
        }

        private fun waitToTomorrow() {
            MiscUtil.sleepMillis(TimeUtil.calculateToTomorrowMillis())
        }

        private fun initThreadName() {
            Thread.currentThread().name = THREAD_NAME
        }
    }
}
