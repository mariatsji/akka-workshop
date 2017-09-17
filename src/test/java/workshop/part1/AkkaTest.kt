package workshop.part1

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.TestProbe
import org.junit.After
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

abstract class AkkaTest {

    protected var system = ActorSystem.create()
    protected var sender = TestProbe.apply(system)

    @After
    fun teardown() {
        val verifySystemShutdown = true
        TestKit.shutdownActorSystem(system, Duration.create(5, TimeUnit.SECONDS), verifySystemShutdown)
    }

    protected fun schedule(delay: FiniteDuration, receiver: ActorRef, msg: Any) {
        system.scheduler().scheduleOnce(delay, receiver, msg, system.dispatcher(), sender.ref())
    }
}
