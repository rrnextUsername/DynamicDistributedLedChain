import alice.tuprolog.Theory
import it.unibo.kactor.sysUtil

object QAKChainSysUtils {
    fun createContext(msgContent: String) {
        val content = msgContent.filter { c -> c != '\'' }

        val actName = content.split("|")[0]
        val ctx = content.split("|")[1]
        val act = content.split("|")[2]
        sysUtil.getPrologEngine().addTheory(Theory(ctx))
        sysUtil.getPrologEngine().addTheory(Theory(act))

        val ctxName = sysUtil.solve("qactor($actName,CTX,_)", "CTX")!!
        val hostAddr = sysUtil.solve("context($ctxName,ADDR,_,_)", "ADDR")!!

        if (sysUtil.getContext(ctxName) == null) {
            sysUtil.createTheContext("$ctxName", "$hostAddr")
            sysUtil.addProxyToOtherCtxs(listOf("$ctxName"))
        }
    }
}