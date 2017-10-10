package examples;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class GreetingActor_1 extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(String.class, message -> {
                System.out.println("Hello " + message);
            }).build();
    }

    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("MySystem");

        ActorRef greetingACtor = system.actorOf(Props.create(GreetingActor_1.class), "greeter");
        greetingACtor.tell("Hulk Hogan", ActorRef.noSender());

        // There are better ways to ensure message are received before termination
        Thread.sleep(100);
        system.terminate();
    }
}
