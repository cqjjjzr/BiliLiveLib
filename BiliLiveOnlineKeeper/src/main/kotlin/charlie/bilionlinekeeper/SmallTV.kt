package charlie.bilionlinekeeper

import charlie.bililivelib.danmaku.DanmakuReceiver
import charlie.bililivelib.danmaku.dispatch.GlobalGiftDispatcher
import charlie.bililivelib.danmaku.event.DanmakuAdapter
import charlie.bililivelib.danmaku.event.DanmakuEvent
import charlie.bililivelib.exceptions.BiliLiveException
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

class SmallTV(session: Session,
              private val receiver: DanmakuReceiver = startupNewReceiver(session)) : DanmakuAdapter() {
    private val protocol: SmallTVProtocol = SmallTVProtocol(session)
    private val logger = LogManager.getLogger("smallTV")

    override fun globalGiftEvent(event: DanmakuEvent) {
        val smallTV = event.param as SmallTV
        (protocol.getSmallTVRoom(smallTV.realRoomID) ?: return)
                .joinedSmallTVs
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
        logStart()
    }

    private fun logStart() {
        logger.info(I18n.getString("smallTV.start"))
    }

    fun stop() {
        receiver.removeDanmakuListener(this)
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
        private val countDownMillis: Long
        private val smallTV: SmallTV
        private val logger: Logger

        override fun run() {
            try {
                protocol.apply {
                    joinLottery(smallTV)
                    logJoinLottery(smallTV)
                    Thread.sleep(countDownMillis)
                    logReward(getReward(smallTV.smallTVID))
                }
            } catch(e: InterruptedException) {
                return
            } catch(e: BiliLiveException) {
                logSingleException(e)
            }
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
            this.logger = logger
        }
    }
}

val DEFAULT_ROOM_ID = 459985
fun startupNewReceiver(session: Session): DanmakuReceiver {
    return DanmakuReceiver(Room(DEFAULT_ROOM_ID, session)).apply {
        connect()
    }
}
