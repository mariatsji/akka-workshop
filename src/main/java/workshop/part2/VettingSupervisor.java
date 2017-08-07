package workshop.part2;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import scala.concurrent.duration.Duration;
import workshop.common.ad.Ad;

import static java.util.concurrent.TimeUnit.MINUTES;
import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;

public class VettingSupervisor extends AbstractActor {

    private final VettingActorFactory vettingActorFactory;

    VettingSupervisor(VettingActorFactory vettingActorFactory) {
        this.vettingActorFactory = vettingActorFactory;
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(10, Duration.create(1, MINUTES), DeciderBuilder
            .match(NullPointerException.class, e -> restart())
            .matchAny(e -> escalate())
            .build());
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
            .match(Ad.class, ad -> vettingActorFactory.create(context()).tell(ad, sender()))
            .build();
    }
}
