package examples;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class GreetingActor_2 extends AbstractActor {

    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("MySystem");

        ActorRef greetingActor = system.actorOf(Props.create(GreetingActor_2.class), "greeter");
        greetingActor.tell(new SayHello("Pope Benedict"), ActorRef.noSender());

        // There are better ways to ensure message are received before termination
        Thread.sleep(100);
        system.terminate();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(SayHello.class, hello -> {
                System.out.println("Hello " + hello.name);
                sender().tell(hello.name, self());
            }).build();
    }

    static class SayHello {
        public final String name;

        public SayHello(String n) {
            name = n;
        }
    }
}
