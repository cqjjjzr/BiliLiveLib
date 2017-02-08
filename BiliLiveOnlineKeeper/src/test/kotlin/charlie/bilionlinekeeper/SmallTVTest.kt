package charlie.bilionlinekeeper

import org.junit.Test

class SmallTVTest {
    @Test
    fun test() {
        val smallTV = SmallTV(TestSessionHelper.initSession())
        smallTV.start()
        synchronized(this, {
            (this as Object).wait()
        })
    }
}