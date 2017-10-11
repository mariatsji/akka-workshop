package workshop.part2.supervisor;

import akka.actor.AbstractActor;

public class VettingSupervisor extends AbstractActor {

    private final VettingActorFactory vettingActorFactory;

    public VettingSupervisor(VettingActorFactory vettingActorFactory) {
        this.vettingActorFactory = vettingActorFactory;
    }

    @Override
    // Spawn a new VettingSubActor for every ad, delegate it, and spoof sender
    public Receive createReceive() {
        return receiveBuilder()
            .build();
    }
}
