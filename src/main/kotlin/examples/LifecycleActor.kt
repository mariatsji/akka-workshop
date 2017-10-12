package examples

import akka.actor.UntypedActor
import scala.Option

class LifecycleActor : UntypedActor() {

    override fun preStart() {
        println("preStart() - called) by FIRST actor-instance during startup ")
    }

    override fun postStop() {
        println("postStop() - called by ANY actor-instance during shutdown")
    }

    override fun preRestart(reason: Throwable, message: Option<Any>) {
        println("preRestart() - called on ANY running actor about to be restarted")
    }

    override fun postRestart(reason: Throwable) {
        println("postRestart() - called on a NEW INSTANCE of this actor after restart")
    }

    override fun onReceive(msg: Any?) = when (msg) {
        is Exception -> {
            throw RuntimeException(msg)
        } else -> {
            unhandled(msg)
        }
    }
}
