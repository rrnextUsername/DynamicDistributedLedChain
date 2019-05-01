package actors

import QAKChainSysUtils
import it.unibo.bls.utils.Utils
import it.unibo.kactor.ActorBasic
import it.unibo.kactor.ApplMessage
import it.unibo.kactor.sysUtil
import kotlinx.coroutines.CoroutineScope
import stateMachine.QAKcmds
import stateMachine.TransitionTable

open class StaticChainLinkActor(name: String, scope: CoroutineScope) : ActorBasic(name, scope, true) {

    protected interface StateInterface

    enum class StaticStates : StateInterface {
        INIT,
        UNCHAINED,
        SLEEP,
        SLEEP_TOKEN,
        LIVE,
        LIVE_TOKEN
    }

    private val delay = 750

    protected var state: StateInterface = StaticStates.INIT
    private var lastMessage: ApplMessage? = null
    protected val transitionTable = TransitionTable<StateInterface, String>()

    private lateinit var led: String
    protected var next: String? = null

    private val ctxName = sysUtil.solve("qactor($name,CTX,_)", "CTX")!!
    private val hostAddr = sysUtil.solve("context($ctxName,ADDR,_,_)", "ADDR")!!
    private val hostProt = sysUtil.solve("context($ctxName,_,PROT,_)", "PROT")!!
    private val hostPort = sysUtil.solve("context($ctxName,_,_,PORT)", "PORT")!!
    private val actClass = "$javaClass"

    protected val ctx = "context($ctxName,\"$hostAddr\",\"$hostProt\",$hostPort)."
    protected val act = "qactor($name,$ctxName,\"$actClass\")."

    init {
        transitionTableSetup()
    }

    protected open fun transitionTableSetup() {
        transitionTable.putAction(StaticStates.INIT, QAKcmds.ControlAddLed.id) {
            println("$name:: received add_led -> subscribing led to this control.")
            doAddLed(lastMessage!!)
        }


        transitionTable.putAction(StaticStates.UNCHAINED, QAKcmds.ControlChangeNext.id) {
            println("$name:: received change_next -> updating next.")
            doChangeNext(lastMessage!!)
            doSleep()
        }


        transitionTable.putAction(StaticStates.SLEEP, QAKcmds.ControlStart.id) {
            println("$name:: received start -> starting chain.")
            doLive()
        }
        transitionTable.putAction(StaticStates.SLEEP, QAKcmds.ControlToken.id) {
            println("$name:: received token -> i am now head.")
            doSleepToken()
        }
        transitionTable.putAction(StaticStates.SLEEP, QAKcmds.ControlChangeNext.id) {
            println("$name:: received change_next -> updating next.")
            doChangeNext(lastMessage!!)
            doSleep()
        }


        transitionTable.putAction(StaticStates.SLEEP_TOKEN, QAKcmds.ControlStart.id) {
            println("$name:: received start -> starting chain.")
            doLiveToken()
        }
        transitionTable.putAction(StaticStates.SLEEP_TOKEN, QAKcmds.ControlChangeNext.id) {
            println("$name:: received change_next -> updating next.")
            doChangeNext(lastMessage!!)
            doSleepToken()
        }


        transitionTable.putAction(StaticStates.LIVE, QAKcmds.ControlStop.id) {
            println("$name:: received stop -> stopping chain.")
            doSleep()
        }
        transitionTable.putAction(StaticStates.LIVE, QAKcmds.ControlToken.id) {
            println("$name:: received token -> i am now head.")
            doLiveToken()
        }


        transitionTable.putAction(StaticStates.LIVE_TOKEN, QAKcmds.ControlStop.id) {
            println("$name:: received stop -> stopping chain.")
            doSleepToken()
        }
        transitionTable.putAction(StaticStates.LIVE_TOKEN, QAKcmds.ControlDelayDone.id) {
            println("$name:: received delay_done -> passing the token to next link.")
            doPassToken()
        }
    }

    override suspend fun actorBody(msg: ApplMessage) {
        lastMessage = msg
        print("$name: received $lastMessage | state: $state ")
        transitionTable.action(state, msg.msgId())?.invoke() ?: println(" :: message not recognized -> dropping.")
    }

    //link state machine
    protected suspend fun doLiveToken() {
        state = StaticStates.LIVE_TOKEN

        //step 1
        doTurnOnLed()

        //step 2
        Utils.delay(delay)
        println("$name::coroutine :::: delay over :: state=$state")

        autoMsg(QAKcmds.ControlDelayDone.id, "delay done")
    }

    protected fun doSleepToken() {
        state = StaticStates.SLEEP_TOKEN
    }

    protected suspend fun doPassToken() {

        //step 1
        turnOffLed()

        //step 2
        forward(QAKcmds.ControlToken.id, "token", next!!)

        //step 3
        doLive()
    }

    protected fun doLive() {
        state = StaticStates.LIVE
    }

    protected fun doSleep() {
        state = StaticStates.SLEEP
    }

    protected suspend fun doControlAddToRegistry() {
        val msg = QAKcmds.RegistryAddLink("$name|$ctx|$act")
        emit(msg.id, msg.cmd)
    }

    protected fun doChangeNext(msg: ApplMessage) {
        next = msg.msgContent().filter { c -> c != '\'' }.split("|")[0]

        QAKChainSysUtils.createContext(msg.msgContent())

        println("\n---------------------------------------------------------------------------------------------------------")
        print("$name:: READY")
        println("\n---------------------------------------------------------------------------------------------------------")
    }


    //led control
    private suspend fun doAddLed(msg: ApplMessage) {

        led = msg.msgContent().filter { c -> c != '\'' }.split("|")[0]

        QAKChainSysUtils.createContext(msg.msgContent())

        doUnchained()
    }

    protected suspend fun doUnchained() {
        state = StaticStates.UNCHAINED

        val msg = QAKcmds.RegistryAddLink("$name|$ctx|$act")
        emit(msg.id, msg.cmd)
    }

    protected suspend fun doTurnOnLed() {
        val msg = QAKcmds.LedOn("turn on LED")
        forward(msg.id, msg.cmd, led)
    }

    protected suspend fun turnOffLed() {
        val msg = QAKcmds.LedOff("turn off LED")
        forward(msg.id, msg.cmd, led)
    }
}