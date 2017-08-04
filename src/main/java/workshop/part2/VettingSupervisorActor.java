package workshop.part2;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import workshop.common.ad.Ad;

public class VettingSupervisorActor extends AbstractActor {

    private final VettingActorFactory vettingActorFactory;

    public VettingSupervisorActor(VettingActorFactory vettingActorFactory) {
        this.vettingActorFactory = vettingActorFactory;
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
            .match(Ad.class, ad -> {
                vettingActorFactory.create(context()).tell(ad, sender());
            })
            .build();
    }
}
