package actors

import QAKChainSysUtils
import it.unibo.bls.utils.Utils
import it.unibo.kactor.ActorBasic
import it.unibo.kactor.ApplMessage
import it.unibo.kactor.sysUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import stateMachine.QAKcmds
import stateMachine.TransitionTable

open class StaticChainLinkActor(name: String, scope: CoroutineScope) : ActorBasic(name, scope, true) {
    enum class States {
        INIT,
        SLEEP,
        SLEEP_TOKEN,
        LIVE,
        LIVE_TOKEN
    }

    private val delay = 750

    protected var state = States.INIT
    private var lastMessage: ApplMessage? = null
    protected val transitionTable = TransitionTable<States, String>()

    private val ledList = mutableListOf<String>()
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
        nextSetup()
    }

    protected open fun nextSetup() {
        scope.launch { autoMsg(QAKcmds.ControlAddToRegistry.id, "add to registry") }
    }

    protected open fun transitionTableSetup() {
        //link state machine
        transitionTable.putAction(States.LIVE_TOKEN, QAKcmds.ControlStop.id) {
            println("$name:: received stop -> stopping chain.")
            doSleepToken()
        }
        transitionTable.putAction(States.LIVE_TOKEN, QAKcmds.ControlDelayDone.id) {
            println("$name:: received delay_done -> passing the token to next head.")
            doPassToken()
        }


        transitionTable.putAction(States.SLEEP_TOKEN, QAKcmds.ControlStart.id) {
            println("$name:: received start -> starting chain.")
            doLiveToken()
        }


        transitionTable.putAction(States.LIVE, QAKcmds.ControlStop.id) {
            println("$name:: received stop -> stopping chain.")
            doSleep()
        }
        transitionTable.putAction(States.LIVE, QAKcmds.ControlToken.id) {
            println("$name:: received token -> i am now head.")
            doLiveToken()
        }


        transitionTable.putAction(States.SLEEP, QAKcmds.ControlStart.id) {
            println("$name:: received start -> starting chain.")
            doLive()
        }
        transitionTable.putAction(States.SLEEP, QAKcmds.ControlToken.id) {
            println("$name:: received token -> i am now head.")
            doSleepToken()
        }

        transitionTable.putAction(States.INIT, QAKcmds.ControlAddToRegistry.id) {
            println("$name:: received add_to_registry -> registering myself on the registry.")
            doControlAddToRegistry()
        }
        transitionTable.putAction(States.INIT, QAKcmds.ControlChangeNext.id) {
            println("$name:: received change_next -> updating next.")
            doChangeNext(lastMessage!!)
        }


        transitionTable.putAction(States.SLEEP_TOKEN, QAKcmds.ControlChangeNext.id) {
            println("$name:: received change_next -> updating next.")
            doChangeNext(lastMessage!!)
        }


        transitionTable.putAction(States.SLEEP, QAKcmds.ControlChangeNext.id) {
            println("$name:: received change_next -> updating next.")
            doChangeNext(lastMessage!!)
        }

        States.values().forEach {
            transitionTable.putAction(it, QAKcmds.ControlAddLed.id) {
                println("$name:: received subscribe message from LED -> adding to list.")
                doAddLed(lastMessage!!)
            }
        }

        transitionTable.putAction(States.INIT, QAKcmds.ControlAddLed.id) {
            println("$name:: received change_next -> updating next.")
            doAddLed(lastMessage!!)
        }
        transitionTable.putAction(States.SLEEP, QAKcmds.ControlAddLed.id) {
            println("$name:: received change_next -> updating next.")
            doAddLed(lastMessage!!)
        }
        transitionTable.putAction(States.SLEEP_TOKEN, QAKcmds.ControlAddLed.id) {
            println("$name:: received change_next -> updating next.")
            doAddLed(lastMessage!!)
        }
        transitionTable.putAction(States.LIVE, QAKcmds.ControlAddLed.id) {
            println("$name:: received change_next -> updating next.")
            doAddLed(lastMessage!!)
        }
        transitionTable.putAction(States.LIVE_TOKEN, QAKcmds.ControlAddLed.id) {
            println("$name:: received change_next -> updating next.")
            doAddLed(lastMessage!!)
        }
    }

    override suspend fun actorBody(msg: ApplMessage) {
        lastMessage = msg
        print("$name: received $lastMessage | state: $state ")
        transitionTable.action(state, msg.msgId())?.invoke() ?: println(" :: message not recognized -> dropping.")
    }

    //link state machine
    private suspend fun doLiveToken() {
        state = States.LIVE_TOKEN

        //step 1
        doTurnOnLed()

        //step 2
        GlobalScope.launch {
            Utils.delay(delay)
            println("$name::coroutine :::: delay over :: state=$state")

            autoMsg(QAKcmds.ControlDelayDone.id, "delay done")

        }
    }

    private fun doSleepToken() {
        state = States.SLEEP_TOKEN
    }

    private suspend fun doPassToken() {

        //step 1
        turnOffLed()

        //step 2
        forward(QAKcmds.ControlToken.id, "token", next!!)

        //step 3
        doLive()
    }

    private fun doLive() {
        state = States.LIVE
    }

    private fun doSleep() {
        state = States.SLEEP
    }

    private suspend fun doControlAddToRegistry() {
        state = States.SLEEP

        val msg = QAKcmds.RegistryAddLink("$name|$ctx|$act")
        emit(msg.id, msg.cmd)
    }

    private fun doChangeNext(msg: ApplMessage) {
        next = msg.msgContent().filter { c -> c != '\'' }.split("|")[0]

        QAKChainSysUtils.createContext(msg.msgContent())

        println("\n---------------------------------------------------------------------------------------------------------")
        print("$name:: READY")
        println("\n---------------------------------------------------------------------------------------------------------")
    }


    //led control
    private fun doAddLed(msg: ApplMessage) {
        ledList.add(msg.msgContent().filter { c -> c != '\'' }.split("|")[0])

        QAKChainSysUtils.createContext(msg.msgContent())
    }

    protected suspend fun doTurnOnLed() {
        val msg = QAKcmds.LedOn("turn on LED")
        //forward(msg.id, msg.cmd, led)
        ledList.forEach { forward(msg.id, msg.cmd, it) }
    }

    protected suspend fun turnOffLed() {
        val msg = QAKcmds.LedOff("turn off LED")
        //forward(msg.id, msg.cmd, led)
        ledList.forEach { forward(msg.id, msg.cmd, it) }
    }
}