package examples;

import java.util.concurrent.TimeUnit;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

public class GreetingActor_3 extends AbstractActor {

    static class DoGreeting {
    }

    @Override
    public void preStart() throws Exception {
        scheduleNextGreeting();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(DoGreeting.class, m -> {
                System.out.println("Hello!");
                scheduleNextGreeting();
            }).build();
    }

    private void scheduleNextGreeting() {
        ActorSystem system = context().system();
        system.scheduler().scheduleOnce(
            Duration.create(1, TimeUnit.SECONDS), self(), new DoGreeting(), system.dispatcher(), self());
    }

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("MySystem");
        system.actorOf(Props.create(GreetingActor_3.class), "greeter");
    }
}