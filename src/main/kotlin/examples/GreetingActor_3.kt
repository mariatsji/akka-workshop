package examples

import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.UntypedActor
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit


class GreetingActor_3 : UntypedActor() {

    internal class DoGreeting

    override fun preStart() {
        scheduleNextGreeting()
    }

    override fun onReceive(msg: Any?) = when (msg) {
        is DoGreeting -> {
            println("Hello!")
            scheduleNextGreeting()
        }
        else -> {
            unhandled(msg)
        }
    }

    private fun scheduleNextGreeting() {
        val system = context().system()
        system.scheduler().scheduleOnce(
                Duration.create(1, TimeUnit.SECONDS), self(), DoGreeting(), system.dispatcher(), self())
    }
}

fun main(args: Array<String>) {
    val system = ActorSystem.create("MySystem")
    system.actorOf(Props.create(GreetingActor_3::class.java), "greeter")
}