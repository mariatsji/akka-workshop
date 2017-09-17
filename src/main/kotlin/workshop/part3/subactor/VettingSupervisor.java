package workshop.part3.subactor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.Terminated;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;
import scala.runtime.BoxedUnit;
import workshop.common.ad.Ad;

import static java.util.concurrent.TimeUnit.MINUTES;
import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;

public class VettingSupervisor extends AbstractActor {

    private final VettingActorFactory vettingActorFactory;
    private ActorRef vettingActor;

    public VettingSupervisor(VettingActorFactory vettingActorFactory) {
        this.vettingActorFactory = vettingActorFactory;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        if (vettingActor == null) {
            vettingActor = vettingActorFactory.create(context());
            context().watch(vettingActor);
        }
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(10, Duration.create(1, MINUTES), DeciderBuilder
            .match(NullPointerException.class, e -> restart())
            .matchAny(e -> escalate())
            .build());
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.create()
                .match(Terminated.class, t -> System.out.println("VettingActor was terminated!"))
                .match(Ad.class, ad -> vettingActor.tell(ad, sender()))
                .build();
    }

}
