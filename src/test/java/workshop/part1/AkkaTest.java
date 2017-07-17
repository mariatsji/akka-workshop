package workshop.part1;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorSystem;
import akka.testkit.TestKit;
import akka.testkit.TestProbe;
import org.junit.After;
import org.junit.Before;
import scala.concurrent.duration.Duration;

public abstract class AkkaTest {

    ActorSystem system;
    TestProbe sender;

    @Before
    public void setup() {
        system = ActorSystem.create();
        sender = TestProbe.apply(system);
    }

    @After
    public void teardown() {
        boolean verifySystemShutdown = true;
        TestKit.shutdownActorSystem(system, Duration.create(5, TimeUnit.SECONDS), verifySystemShutdown);
    }

}
