package workshop.examples;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import javaslang.control.Try;

public class HelloWorldActor extends AbstractActor {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("MyActorSystem");
        final ActorRef myActor = system.actorOf(Props.create(HelloWorldActor.class), "helloWorld");

        Message msg = new Message("1", "1", "Hello newborn Actor!");
        myActor.tell(msg, ActorRef.noSender());

        Try.of(() -> {
            Thread.sleep(200);
            return system.terminate();
        });
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create().match(
                Message.class, m -> System.out.println(String.format("Got Message %s", m)))
                .build();
    }


}
