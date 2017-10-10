package examples;

import akka.actor.AbstractActor;

class TestActor extends AbstractActor {
    @Override
    public void preStart() throws Exception {
        System.out.println("Created TestActor");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(String.class, System.out::println).build();
    }
}