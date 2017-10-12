package examples

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.UntypedActor

class GreetingActor_1 : UntypedActor() {

    override fun onReceive(msg: Any?) = when (msg) {
        is String -> {
            System.out.println("Hello " + msg)
        }
        else -> {
            unhandled(msg)
        }
    }
}

fun main(args: Array<String>) {
    val system = ActorSystem.create("MySystem")

    val greetingACtor = system.actorOf(Props.create(GreetingActor_1::class.java), "greeter")
    greetingACtor.tell("Hulk Hogan", ActorRef.noSender())

    // There are better ways to ensure message are received before termination
    Thread.sleep(100)
    system.terminate()
}
