package examples;

import akka.actor.AbstractActor;

public class VolatileGreetingActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class, s -> context().stop(self())).build();
    }
}
