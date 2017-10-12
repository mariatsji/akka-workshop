package workshop.part3.routers

import akka.actor.*
import akka.actor.SupervisorStrategy.*
import akka.japi.pf.DeciderBuilder
import akka.japi.pf.ReceiveBuilder
import akka.routing.ActorRefRoutee
import akka.routing.RoundRobinRoutingLogic
import akka.routing.Routee
import akka.routing.Router
import scala.concurrent.duration.Duration
import workshop.common.ad.Ad
import workshop.common.fraudwordsservice.FraudWordService
import workshop.common.userservice.UserService
import workshop.part2.FraudWordActor
import workshop.part2.UserActor
import workshop.part2.supervisor.UserNotFoundException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MINUTES


/**
 * No tests for this implementation - you are free to code like it is a party, and the year is 1990
 */
class VettingRouter : AbstractActor() {

    internal var router: Router

    init {
        val routees = listOf(1..5)
                .map { newRoutee() }

        router = Router(RoundRobinRoutingLogic(), routees)
    }

    private fun newRoutee(): Routee {
        val userActor = context.actorOf(Props.create(UserActor::class.java) { UserActor(UserService()) })
        val fraudWordActor = context.actorOf(Props.create(FraudWordActor::class.java) { FraudWordActor(FraudWordService()) })
        val routee = context.actorOf(Props.create(VettingActor::class.java
        ) { VettingActor(userActor, fraudWordActor, Duration.create(1, TimeUnit.SECONDS)) })
        context.watch(routee)
        return ActorRefRoutee(routee)
    }

    override fun supervisorStrategy(): SupervisorStrategy {
        return OneForOneStrategy(10, Duration.create(1, MINUTES), DeciderBuilder
                .match(UserNotFoundException::class.java) { e -> resume() }
                .match(NullPointerException::class.java) { e -> restart() }
                .matchAny { e -> escalate() }
                .build())
    }

    override fun createReceive(): AbstractActor.Receive {
        return ReceiveBuilder.create()
                .match(Terminated::class.java) { t ->
                    router.removeRoutee(t.actor)
                    router = router.addRoutee(newRoutee())
                }
                .match(Ad::class.java) { ad -> router.route(ad, sender) }
                .build()
    }


}
