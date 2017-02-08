package charlie.bilionlinekeeper

import charlie.bililivelib.exceptions.BiliLiveException
import charlie.bililivelib.user.Session
import charlie.bililivelib.user.SignProtocol
import charlie.bilionlinekeeper.util.I18n
import charlie.bilionlinekeeper.util.LogUtil
import charlie.bilionlinekeeper.util.TimeUtil
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

class SignIn(session: Session) : Runnable {
    private val protocol = SignProtocol(session)
    private val logger = LogManager.getLogger("signIn")
    private var thread = Thread()

    override fun run() {
        startupThread()
        while (!Thread.currentThread().isInterrupted) {
            try {
                logSign(protocol.signIn())
            } catch(e: BiliLiveException) {
                logSingleException(e)
            }

            try {
                Thread.sleep(TimeUtil.calculateToTomorrowMillis())
            } catch(e: InterruptedException) {
                break
            }
        }
    }

    private fun logSign(signIn: SignProtocol.DoSignInfo) {
        if (!checkSignInSuccessfully(signIn)) return
        logger.info(I18n.format(
                "signIn.signedIn",
                signIn.data.text,
                signIn.data.hadSignDays))
    }

    private fun checkSignInSuccessfully(signIn: SignProtocol.DoSignInfo): Boolean {
        if (signIn.isSuccessful) return true
        if (signIn.isAlreadySignedIn) logger.warn(I18n.getString("signIn.alreadySignedIn"))
        return false
    }

    private fun logSingleException(e: BiliLiveException) {
        LogUtil.logException(
                logger,
                Level.WARN,
                I18n.getString("signIn.singleException")!!,
                e)
    }

    private fun startupThread() {
        Thread.currentThread().name = "SignIn"
    }

    fun start() {
        thread = Thread(this)
        thread.start()
    }

    fun stop() {
        thread.interrupt()
    }

    fun isStarted(): Boolean = thread.isAlive
}