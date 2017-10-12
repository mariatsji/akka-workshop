package examples

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.UntypedActor

class SayHello(val name: String)

class GreetingActor_2 : UntypedActor() {

    override fun onReceive(msg: Any?) = when (msg) {
        is SayHello -> {
            println("Hello " + msg.name)
            sender().tell(msg.name, self())
        }
        else -> {
            unhandled(msg)
        }
    }
}

fun main(args: Array<String>) {
    val system = ActorSystem.create("MySystem")

    val greetingActor = system.actorOf(Props.create(GreetingActor_2::class.java), "greeter")
    greetingActor.tell(SayHello("Pope Benedict"), ActorRef.noSender())

    // There are better ways to ensure message are received before termination
    Thread.sleep(100)
    system.terminate()
}