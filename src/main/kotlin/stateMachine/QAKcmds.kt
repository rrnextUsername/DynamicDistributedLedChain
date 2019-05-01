package stateMachine


sealed class QAKcmds(val cmd: String, val id: String = "QAKcmds") {

    //BUTTON MESSAGES
    class ButtonClicked(cmd: String = id) : QAKcmds(cmd, id = id) {
        companion object {
            const val id = "ButtonClicked"
        }
    }

    //LED MESSAGES
    class LedOn(cmd: String = id) : QAKcmds(cmd, id = id) {
        companion object {
            const val id = "LedOn"
        }
    }

    class LedOff(cmd: String = id) : QAKcmds(cmd, id = id) {
        companion object {
            const val id = "LedOff"
        }
    }


    //CHAIN_LINK MESSAGES
    class ControlStart(cmd: String = id) : QAKcmds(cmd, id = id) {
        companion object {
            const val id = "ControlStart"
        }
    }

    class ControlStop(cmd: String = id) : QAKcmds(cmd, id = id) {
        companion object {
            const val id = "ControlStop"
        }
    }

    class ControlToken(cmd: String = id) : QAKcmds(cmd, id = id) {
        companion object {
            const val id = "ControlToken"
        }
    }

    class ControlDelayDone(cmd: String = id) : QAKcmds(cmd, id = id) {
        companion object {
            const val id = "ControlDelayDone"
        }
    }

    class ControlChangeNext(cmd: String) : QAKcmds(cmd, id = id) {
        companion object {
            const val id = "ControlChangeNext"
        }
    }

    class ControlRemoveFromRegistry(cmd: String = id) : QAKcmds(cmd, id = id) {
        companion object {
            const val id = "ControlRemoveFromRegistry"
        }
    }

    class ControlAddToRegistry(cmd: String = id) : QAKcmds(cmd, id = id) {
        companion object {
            const val id = "ControlAddToRegistry"
        }
    }

    class ControlAddLed(cmd: String = id) : QAKcmds(cmd, id = id) {
        companion object {
            const val id = "ControlAddLed"
        }
    }

    //REGISTRY MESSAGES
    class RegistryAddLink(cmd: String = id) : QAKcmds(cmd, id = id) {
        companion object {
            const val id = "RegistryAddLink"
        }
    }

    class RegistryRemoveLink(cmd: String = id) : QAKcmds(cmd, id = id) {
        companion object {
            const val id = "RegistryRemoveLink"
        }
    }

    class RegistryButtonClicked(cmd: String = id) : QAKcmds(cmd, id = id) {
        companion object {
            const val id = "RegistryButtonClicked"
        }
    }


    override fun toString(): String {
        return "$id($cmd)"
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is QAKcmds -> id == other.id && cmd == other.cmd
            else -> false
        }
    }
}