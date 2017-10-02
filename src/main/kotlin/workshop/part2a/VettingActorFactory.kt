package workshop.part2a

import akka.actor.ActorContext
import akka.actor.ActorRef
import akka.actor.Props
import scala.concurrent.duration.Duration
import workshop.part2b.subactor.VettingActor
import java.util.concurrent.TimeUnit

class VettingActorFactory(private val userActor: ActorRef, private val fraudWordActor: ActorRef) {

    fun create(context: ActorContext): ActorRef {
        return context.system().actorOf(Props.create(VettingActor::class.java
        ) { VettingActor(userActor, fraudWordActor, Duration.create(1, TimeUnit.SECONDS)) })
    }
}