package workshop.part1;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import javaslang.control.Try;

public class Main extends AbstractActor {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("MonolithActorSystem");
        final ActorRef myActor = system.actorOf(Props.create(FraudControlActor.class), "fraudControl");

        Try.of(() -> {
            Thread.sleep(200);
            return system.terminate();
        });
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
