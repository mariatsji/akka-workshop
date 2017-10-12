package examples

import akka.actor.*
import akka.actor.SupervisorStrategy.*
import akka.japi.pf.DeciderBuilder
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit.MINUTES


class TestActor : UntypedActor() {

    override fun preStart() {
        println("Created test actor")
    }

    override fun onReceive(msg: Any?) = unhandled(msg)
}

class SupervisorActor : UntypedActor() {

    override fun supervisorStrategy(): SupervisorStrategy {
        val loggingEnabled = false
        return OneForOneStrategy(10, Duration.create(1, MINUTES), loggingEnabled, DeciderBuilder
                .match(ArithmeticException::class.java) { e -> resume() }
                .match(NullPointerException::class.java) { e -> restart() }
                .match(IllegalArgumentException::class.java) { e -> stop() }
                .matchAny { o -> escalate() }.build())
    }

    override fun onReceive(msg: Any?) = when (msg) {
        is Props -> {
            val testActor = context().actorOf(msg)
            testActor.tell("print this string, please", self())
        }
        else -> {
            unhandled(msg)
        }
    }
}

fun main(args: Array<String>) {
    val system = ActorSystem.create("MySystem")

    val supervisor = system.actorOf(Props.create(SupervisorActor::class.java), "supervisor")
    val testActorProps = Props.create(TestActor::class.java)
    supervisor.tell(testActorProps, ActorRef.noSender())

    // There are better ways to ensure message are received before termination
    Thread.sleep(100)
    system.terminate()
}