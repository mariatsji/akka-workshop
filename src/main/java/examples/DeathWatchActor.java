package examples;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Terminated;

public class DeathWatchActor extends AbstractActor {

    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("MySystem");
        system.actorOf(Props.create(DeathWatchActor.class), "deathWatchActor");

        // There are better ways to ensure message are received before termination
        Thread.sleep(100);
        system.terminate();
    }

    @Override
    public void preStart() throws Exception {
        ActorRef greetingActor = context().actorOf(Props.create(VolatileGreetingActor.class), "volatileActor");
        context().watch(greetingActor);
        greetingActor.tell("print this message, please!", self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(Terminated.class, s -> {
                    System.out.println("Looks like an actor has died");
                    context().stop(self());
                }
            ).build();
    }
}
