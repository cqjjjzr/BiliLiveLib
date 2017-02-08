package charlie.bilionlinekeeper

import org.junit.Test

class OnlineExpTest {
    @Test
    fun test() {
        OnlineExp(TestSessionHelper.initSession()).start()
        synchronized(this, {
            (this as Object).wait()
        })
    }
}