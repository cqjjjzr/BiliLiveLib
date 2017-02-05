package charlie.bilionlinekeeper

import charlie.bililivelib.danmaku.DanmakuReceiver
import charlie.bililivelib.danmaku.dispatch.GlobalGiftDispatcher
import charlie.bililivelib.danmaku.event.DanmakuAdapter
import charlie.bililivelib.danmaku.event.DanmakuEvent
import charlie.bililivelib.exceptions.BiliLiveException
import charlie.bililivelib.room.Room
import charlie.bililivelib.smalltv.SmallTV
import charlie.bililivelib.smalltv.SmallTVProtocol
import charlie.bililivelib.user.Session
import charlie.bilionlinekeeper.util.I18n
import charlie.bilionlinekeeper.util.LogUtil
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class SmallTV(session: Session, receiver: DanmakuReceiver = startupNewReceiver(session)) : DanmakuAdapter() {
    private val protocol: SmallTVProtocol = SmallTVProtocol(session)
    private val logger = LogManager.getLogger("SmallTV")

    override fun globalGiftEvent(event: DanmakuEvent) {
        val smallTV = event.param as SmallTV
        (protocol.getSmallTVRoom(smallTV.realRoomID) ?: return)
                .joinedSmallTVs
                .filter { it.smallTVID == smallTV.smallTVID }
                .apply {
                    if (isEmpty()) return
                    Thread(SingleSmallTV(first().countDown, smallTV, logger)).start()
                }
    }

    init {
        checkReceiverDispatchManager(receiver)
        receiver.addDanmakuListener(this)
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
                protocol.joinLottery(smallTV)
                Thread.sleep(countDownMillis)
                protocol.getReward(smallTV.smallTVID)
            } catch(e: InterruptedException) {
                return
            } catch(e: BiliLiveException) {
                logSingleException(e)
            }
        }

        private fun logSingleException(e: BiliLiveException) {
            LogUtil.logException(logger, Level.WARN, I18n.getString("smallTV.single_exception")!!, e)
        }

        init {
            this.countDownMillis = (countDownSeconds * 1000).toLong()
            this.smallTV = smallTV
            this.logger = logger
        }
    }
}

fun startupNewReceiver(session: Session): DanmakuReceiver {
    return DanmakuReceiver(Room(2, session)).apply {
        connect()
    }
}
