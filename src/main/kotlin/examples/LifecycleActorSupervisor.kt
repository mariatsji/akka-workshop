package examples

import akka.actor.*
import akka.actor.SupervisorStrategy.restart
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MINUTES

class CreateLifeCycleActor

class LifecycleActorSupervisor : UntypedActor() {


    override fun supervisorStrategy(): SupervisorStrategy {
        return OneForOneStrategy(10, Duration.create(1, MINUTES),
                { t -> restart() }, false)
    }

    override fun onReceive(msg: Any?) = when (msg) {
        is CreateLifeCycleActor -> {
            val actorRef = context().actorOf(Props.create(LifecycleActor::class.java), "lifecycleActor")
            sender().tell(actorRef, self())
        } else -> {
            unhandled(msg)
        }
    }

}

fun main(args: Array<String>) {
    val system = ActorSystem.create("MySystem")

    val supervisor = system.actorOf(Props.create(LifecycleActorSupervisor::class.java), "lifecycleSupervisor")
    val inbox = Inbox.create(system)
    inbox.send(supervisor, CreateLifeCycleActor())

    val child = inbox.receive(Duration.create(1, TimeUnit.MINUTES)) as ActorRef
    child.tell(RuntimeException("Ay, caramba!"), ActorRef.noSender())
    child.tell(PoisonPill.getInstance(), ActorRef.noSender())

    // There are better ways to ensure message are received before termination
    Thread.sleep(100)
    system.terminate()
}
