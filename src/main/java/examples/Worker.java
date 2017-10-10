package examples;

import akka.actor.AbstractActor;

public class Worker extends AbstractActor{

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class, s -> sender().tell(s.length(), self())).build();
    }

}
