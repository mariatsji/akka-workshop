package workshop.part2.supervisor

import akka.actor.ActorContext
import akka.actor.ActorRef
import akka.actor.Props
import scala.concurrent.duration.Duration
import workshop.part2.subactor.VettingSubActor
import java.util.concurrent.TimeUnit

class VettingActorFactory(private val userActor: ActorRef, private val fraudWordActor: ActorRef) {

    fun create(context: ActorContext): ActorRef {
        return context.system().actorOf(Props.create(VettingSubActor::class.java
        ) { VettingSubActor(userActor, fraudWordActor, Duration.create(1, TimeUnit.SECONDS)) })
    }
}