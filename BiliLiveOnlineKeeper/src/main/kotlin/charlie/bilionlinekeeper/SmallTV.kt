package charlie.bilionlinekeeper

import charlie.bililivelib.danmaku.DanmakuReceiver
import charlie.bililivelib.danmaku.dispatch.GlobalGiftDispatcher
import charlie.bililivelib.danmaku.event.DanmakuAdapter
import charlie.bililivelib.danmaku.event.DanmakuEvent
import charlie.bililivelib.exceptions.BiliLiveException
import charlie.bililivelib.internalutil.MiscUtil
import charlie.bililivelib.room.Room
import charlie.bililivelib.smalltv.SmallTV
import charlie.bililivelib.smalltv.SmallTVProtocol
import charlie.bililivelib.smalltv.SmallTVReward
import charlie.bililivelib.user.Session
import charlie.bilionlinekeeper.util.I18n
import charlie.bilionlinekeeper.util.LogUtil
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*

class SmallTV(session: Session,
              private val receiver: DanmakuReceiver = startupNewReceiver(session),
              private val reconnectTimeMillis: Long = 0L) : DanmakuAdapter() {
    private val protocol: SmallTVProtocol = SmallTVProtocol(session)
    private val logger = LogManager.getLogger("smallTV")
    private var reconnectTask: TimerTask = AutoReconnectTask()
    private val timer = Timer("SmallTV-ReconnectTask")

    override fun globalGiftEvent(event: DanmakuEvent) {
        val smallTV = event.param as SmallTV
        (protocol.getSmallTVRoom(smallTV.realRoomID) ?: return)
                .notJoinedSmallTVs
                .filter { it.smallTVID == smallTV.smallTVID }
                .apply {
                    if (isEmpty()) return
                    Thread(SingleSmallTV(first().countDownSecond, smallTV, logger)).start()
                }
        //println(event.param.toString())
    }

    override fun errorEvent(event: DanmakuEvent) {
        if (event.kind == DanmakuEvent.Kind.ERROR_DOWN) {
            logger.error(I18n.getString("smallTV.error_down"))
            stop()
        }
    }

    fun start() {
        receiver.addDanmakuListener(this)
        if (reconnectTimeMillis != 0L) {
            reconnectTask = AutoReconnectTask()
            timer.schedule(reconnectTask, reconnectTimeMillis, reconnectTimeMillis)
        }
        logStart()
    }

    private fun logStart() {
        logger.info(I18n.getString("smallTV.start"))

    }

    fun stop() {
        receiver.removeDanmakuListener(this)
        if (reconnectTimeMillis != 0L) {
            reconnectTask.cancel()
        }
        logStop()
    }

    private fun logStop() {
        logger.info(I18n.getString("smallTV.stop"))
    }

    fun isStarted(): Boolean = receiver.hasDanmakuListener(this)

    init {
        checkReceiverDispatchManager(receiver)
    }

    private fun checkReceiverDispatchManager(receiver: DanmakuReceiver) {
        if (!receiver.dispatchManager.isDispatcherPresented(GlobalGiftDispatcher::class.java)) {
            receiver.dispatchManager.registerDispatcher(GlobalGiftDispatcher.getGlobalInstance())
        }
    }

    private inner class SingleSmallTV(countDownSeconds: Int, smallTV: SmallTV, logger: Logger) : Runnable {
        private val WAIT_TIME_MILLIS = 5000L

        private val countDownMillis: Long
        private val smallTV: SmallTV

        override fun run() {
            startupThread()
            try {
                protocol.apply {
                    joinLottery(smallTV)
                    logJoinLottery(smallTV)
                    Thread.sleep(countDownMillis)
                    var reward = getReward(smallTV.smallTVID)
                    while (reward.isStillDrawing) {
                        MiscUtil.sleepMillis(WAIT_TIME_MILLIS)
                        reward = getReward(smallTV.smallTVID)
                    }
                    logReward(reward)
                }
            } catch(e: InterruptedException) {
                return
            } catch(e: BiliLiveException) {
                logSingleException(e)
            }
        }

        private fun startupThread() {
            Thread.currentThread().name = "SmallTV-Wait-${smallTV.smallTVID}"
        }

        private fun logReward(reward: SmallTVReward) {
            if (!isRewardValid(reward)) return
            (reward.reward ?: return).apply {
                logger.info(
                        I18n.format(
                                "smallTV.gotReward",
                                smallTV.realRoomID,
                                kind.displayName,
                                count)
                )
            }
        }

        private fun isRewardValid(reward: SmallTVReward): Boolean {
            return reward.code == 0 && !reward.isMiss
        }

        private fun logJoinLottery(smallTV: SmallTV) {
            logger.info(I18n.format(
                    "smallTV.join",
                    smallTV.smallTVID,
                    smallTV.realRoomID))
        }

        private fun logSingleException(e: BiliLiveException) {
            LogUtil.logException(
                    logger,
                    Level.WARN,
                    I18n.format(
                            "smallTV.single_exception",
                            smallTV.smallTVID)!!,
                    e)
        }

        init {
            this.countDownMillis = (countDownSeconds * 1000).toLong()
            this.smallTV = smallTV
        }
    }

    private inner class AutoReconnectTask : TimerTask() {
        override fun run() {
             if (isStarted()) {
                 receiver.disconnect()
                 receiver.connect()
                 logger.debug("Reset receiver!")
             }
        }
    }
}

val DEFAULT_ROOM_ID = 459985
fun startupNewReceiver(session: Session): DanmakuReceiver {
    return DanmakuReceiver(Room(DEFAULT_ROOM_ID, session)).apply {
        connect()
    }
}
