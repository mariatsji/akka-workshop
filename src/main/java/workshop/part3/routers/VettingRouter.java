package workshop.part3.routers;

import java.util.concurrent.TimeUnit;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.Terminated;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import javaslang.collection.List;
import scala.concurrent.duration.Duration;
import workshop.common.ad.Ad;
import workshop.common.fraudwordsservice.FraudWordService;
import workshop.common.userservice.UserService;
import workshop.part2.FraudWordActor;
import workshop.part2.UserActor;
import workshop.part2.supervisor.UserNotFoundException;

import static java.util.concurrent.TimeUnit.MINUTES;
import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.resume;


/**
 * No tests for this implementation - you are free to code like it is a party, and the year is 1990
 */
public class VettingRouter extends AbstractActor {

    Router router;

    {
        List<Routee> routees = List.range(0, 5).map(i -> newRoutee());

        router = new Router(new RoundRobinRoutingLogic(), routees);
    }

    private Routee newRoutee() {
        ActorRef userActor = getContext().actorOf(Props.create(UserActor.class, () -> new UserActor(new UserService())));
        ActorRef fraudWordActor = getContext().actorOf(Props.create(FraudWordActor.class, () -> new FraudWordActor(new FraudWordService())));
        ActorRef routee = getContext().actorOf(Props.create(VettingActor.class,
                () -> new VettingActor(userActor, fraudWordActor, Duration.create(1, TimeUnit.SECONDS))));
        getContext().watch(routee);
        return new ActorRefRoutee(routee);
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(10, Duration.create(1, MINUTES), DeciderBuilder
                .match(UserNotFoundException.class, e -> resume())
                .match(NullPointerException.class, e -> restart())
                .matchAny(e -> escalate())
                .build());
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(Terminated.class, t -> {
                    router.removeRoutee(t.getActor());
                    router = router.addRoutee(newRoutee());
                })
                .match(Ad.class, ad -> router.route(ad, getSender()))
                .build();
    }


}
