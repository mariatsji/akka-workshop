package workshop.part1;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import akka.testkit.TestProbe;
import org.junit.After;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

public abstract class AkkaTest {

    protected ActorSystem system = ActorSystem.create();
    protected TestProbe sender = TestProbe.apply(system);

    @After
    public void teardown() {
        boolean verifySystemShutdown = true;
        TestKit.shutdownActorSystem(system, Duration.create(5, TimeUnit.SECONDS), verifySystemShutdown);
    }

    protected void schedule(FiniteDuration delay, ActorRef receiver, Object msg) {
        system.scheduler().scheduleOnce(delay, receiver, msg, system.dispatcher(), sender.ref());
    }
}
