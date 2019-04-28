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

class ChainLinkActor(name: String, scope: CoroutineScope) : ActorBasic(name, scope, true) {
    enum class States {
        INIT,
        SLEEP,
        SLEEP_TOKEN,
        LIVE,
        LIVE_TOKEN
    }

    private val delay = 750

    private var state = States.INIT
    private var lastMessage: ApplMessage? = null
    private val transitionTable = TransitionTable<States, String>()

    private val led = LedSegmentActor("ledActor_$name", scope)
    private var next: String? = null

    init {
        transitionTableSetup()

        scope.launch { autoMsg(QAKcmds.ControlAddToRegistry.id, "add to registry") }
    }

    private fun transitionTableSetup() {
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


        //dynamic chain
        transitionTable.putAction(States.INIT, QAKcmds.ControlAddToRegistry.id) {
            println("$name:: received add_to_registry -> registering myself on the registry.")
            doControlAddToRegistry()
        }
        transitionTable.putAction(States.INIT, QAKcmds.ControlChangeNext.id) {
            println("$name:: received change_next -> updating next.")
            doChangeNext(lastMessage!!)
        }


        transitionTable.putAction(States.LIVE_TOKEN, QAKcmds.ControlChangeNext.id) {
            println("$name:: received change_next -> updating next.")
            doChangeNext(lastMessage!!)
        }
        transitionTable.putAction(States.LIVE_TOKEN, QAKcmds.ControlRemoveFromRegistry.id) {
            println("$name:: received remove_from_registry -> de-registering myself from the registry and passing token ahead.")

            forward(QAKcmds.ControlStop.id, "removing myself -> stop chain momentarily", next!!)
            doControlRemoveFromRegistry()
            forward(QAKcmds.ControlToken.id, "removing myself -> passing token ahead", next!!)
            forward(QAKcmds.ControlStart.id, "removing myself ->starting chain again", next!!)
        }


        transitionTable.putAction(States.SLEEP_TOKEN, QAKcmds.ControlChangeNext.id) {
            println("$name:: received change_next -> updating next.")
            doChangeNext(lastMessage!!)
        }
        transitionTable.putAction(States.SLEEP_TOKEN, QAKcmds.ControlRemoveFromRegistry.id) {
            println("$name:: received remove_from_registry -> de-registering myself from the registry and passing token ahead.")

            doControlRemoveFromRegistry()
            forward(QAKcmds.ControlToken.id, "removing myself -> passing token ahead", next!!)
        }


        transitionTable.putAction(States.LIVE, QAKcmds.ControlChangeNext.id) {
            println("$name:: received change_next -> updating next.")
            doChangeNext(lastMessage!!)
        }
        transitionTable.putAction(States.LIVE, QAKcmds.ControlRemoveFromRegistry.id) {
            println("$name:: received remove_from_registry -> de-registering myself from the registry.")

            forward(QAKcmds.ControlStop.id, "removing myself -> stop chain momentarily", next!!)
            doControlRemoveFromRegistry()
            forward(QAKcmds.ControlStart.id, "removing myself ->starting chain again", next!!)
        }


        transitionTable.putAction(States.SLEEP, QAKcmds.ControlChangeNext.id) {
            println("$name:: received change_next -> updating next.")
            doChangeNext(lastMessage!!)
        }
        transitionTable.putAction(States.SLEEP, QAKcmds.ControlRemoveFromRegistry.id) {
            println("$name:: received remove_from_registry -> de-registering myself from the registry.")

            doControlRemoveFromRegistry()
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

    private suspend fun doSleepToken() {
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

    private suspend fun doLive() {
        state = States.LIVE
    }

    private suspend fun doSleep() {
        state = States.SLEEP
    }

    //dynamic chain
    private suspend fun doControlAddToRegistry() {
        state = States.SLEEP

        val ctxName = sysUtil.solve("qactor($name,CTX,_)", "CTX")!!
        val hostAddr = sysUtil.solve("context($ctxName,ADDR,_,_)", "ADDR")!!
        val hostProt = sysUtil.solve("context($ctxName,_,PROT,_)", "PROT")!!
        val hostPort = sysUtil.solve("context($ctxName,_,_,PORT)", "PORT")!!

        val actClass = "$javaClass"

        val ctx = "context($ctxName,\"$hostAddr\",\"$hostProt\",$hostPort)."
        val act = "qactor($name,$ctxName,\"$actClass\")."

        val msg = QAKcmds.RegistryAddLink("$name|$ctx|$act")
        emit(msg.id, msg.cmd)
    }

    private suspend fun doControlRemoveFromRegistry() {
        state = States.INIT

        val ctxName = sysUtil.solve("qactor($name,CTX,_)", "CTX")!!
        val hostAddr = sysUtil.solve("context($ctxName,ADDR,_,_)", "ADDR")!!
        val hostProt = sysUtil.solve("context($ctxName,_,PROT,_)", "PROT")!!
        val hostPort = sysUtil.solve("context($ctxName,_,_,PORT)", "PORT")!!

        val actClass = "$javaClass"

        val ctx = "context($ctxName,\"$hostAddr\",\"$hostProt\",$hostPort)."
        val act = "qactor($name,$ctxName,\"$actClass\")."

        val msg = QAKcmds.RegistryRemoveLink("$name|$ctx|$act")
        emit(msg.id, msg.cmd)

        turnOffLed()
    }

    private fun doChangeNext(msg: ApplMessage) {
        next = msg.msgContent().filter { c -> c != '\'' }.split("|")[0]

        QAKChainSysUtils.createContext(msg.msgContent())

        println("\n---------------------------------------------------------------------------------------------------------")
        print("$name:: READY")
        println("\n---------------------------------------------------------------------------------------------------------")
    }


    //led control
    private suspend fun doTurnOnLed() {
        val msg = QAKcmds.LedOn("turn on LED")
        forward(msg.id, msg.cmd, led)
    }

    private suspend fun turnOffLed() {
        val msg = QAKcmds.LedOff("turn off LED")
        forward(msg.id, msg.cmd, led)
    }
}