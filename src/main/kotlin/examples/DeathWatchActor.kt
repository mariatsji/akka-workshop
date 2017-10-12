package examples

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.UntypedActor

class VolatileGreetingActor : UntypedActor() {

    override fun onReceive(msg: Any?) = context().stop(self())
}

class DeathWatchActor : UntypedActor() {

    override fun preStart() {
        val greetingActor = context().actorOf(Props.create(VolatileGreetingActor::class.java), "volatileActor")
        context().watch(greetingActor)
        greetingActor.tell("print this message, please!", self())
    }

    override fun onReceive(msg: Any?) = when (msg) {
        is Terminated -> {
            println("Looks like an actor has died")
            context().stop(self())
        }
        else -> {
            unhandled(msg)
        }
    }
}

fun main(args: Array<String>) {
    val system = ActorSystem.create("MySystem")
    system.actorOf(Props.create(DeathWatchActor::class.java), "deathWatchActor")

    // There are better ways to ensure message are received before termination
    Thread.sleep(100)
    system.terminate()
}
