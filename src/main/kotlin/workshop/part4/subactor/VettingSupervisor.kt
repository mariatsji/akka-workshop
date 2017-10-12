package workshop.part4.subactor

import akka.actor.*
import akka.actor.SupervisorStrategy.escalate
import akka.actor.SupervisorStrategy.restart
import akka.japi.pf.DeciderBuilder
import scala.concurrent.duration.Duration
import workshop.common.ad.Ad
import java.util.concurrent.TimeUnit.MINUTES

class VettingSupervisor(private val vettingActorFactory: VettingActorFactory) : AbstractActor() {
    private var vettingActor: ActorRef? = null

    @Throws(Exception::class)
    override fun preStart() {
        super.preStart()
        if (vettingActor == null) {
            vettingActor = vettingActorFactory.create(context())
            context().watch(vettingActor)
        }
    }

    override fun supervisorStrategy(): SupervisorStrategy {
        return OneForOneStrategy(10, Duration.create(1, MINUTES), DeciderBuilder
                .match(NullPointerException::class.java) { e -> restart() }
                .matchAny { e -> escalate() }
                .build())
    }

    override fun createReceive(): AbstractActor.Receive {
        return receiveBuilder()
                .match(Terminated::class.java) { t -> println("VettingActor was terminated!") }
                .match(Ad::class.java) { ad -> vettingActor!!.tell(ad, sender()) }
                .build()
    }

}
