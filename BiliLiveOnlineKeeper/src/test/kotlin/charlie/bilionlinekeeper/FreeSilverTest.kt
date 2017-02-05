package charlie.bilionlinekeeper

import org.junit.Test

class FreeSilverTest {
    @Test
    fun start() {
        val freeSilver = FreeSilver(TestSessionHelper.initSession())
        freeSilver.start()
        synchronized(this, {
            (this as Object).wait()
        })
    }
}