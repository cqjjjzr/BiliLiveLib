package charlie.bilionlinekeeper

import org.junit.Test

class SmallTVTest {
    @Test
    fun test() {
        val smallTV = SmallTV(TestSessionHelper.initSession(),
                reconnectTimeMillis = 5L * 60L * 1000L)
        smallTV.start()
        synchronized(this, {
            (this as Object).wait()
        })
    }
}