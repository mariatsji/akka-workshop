package workshop.part2a

import akka.actor.ActorRef
import akka.actor.Props
import akka.testkit.JavaTestKit
import akka.testkit.TestActor
import akka.testkit.TestKit
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test
import workshop.common.ad.Ad
import workshop.common.userservice.UserCriminalRecord
import workshop.part1.AkkaTest
import workshop.part1.Verdict
import workshop.part2b.FraudWordActor
import workshop.part2b.UserActor

class VettingSupervisorIntegrationTest : AkkaTest() {

    @Test
    fun handlesVettingRequestsInParallel() {
        object : TestKit(system) { init {
            val userActor = TestKit(system)
            userActor.setAutoPilot(object : TestActor.AutoPilot() {
                override fun run(sender: ActorRef, msg: Any): TestActor.AutoPilot {
                    sender.tell(UserActor.CheckUserResult(UserCriminalRecord.GOOD), ActorRef.noSender())
                    return keepRunning()
                }
            })

            val fraudWordActor = TestKit(system)
            fraudWordActor.setAutoPilot(object : TestActor.AutoPilot() {
                override fun run(sender: ActorRef, msg: Any): TestActor.AutoPilot {
                    sender.tell(FraudWordActor.ExamineWordsResult(emptyList()), ActorRef.noSender())
                    return keepRunning()
                }
            })

            val vettingActorFactory = VettingActorFactory(userActor.testActor(), fraudWordActor.testActor())
            val vettingActor = system.actorOf(Props.create(VettingSupervisor::class.java) { VettingSupervisor(vettingActorFactory) })

            vettingActor.tell(createAd(1), testActor())
            vettingActor.tell(createAd(2), testActor())

            within<Any>(JavaTestKit.duration("3 seconds")) {
                assertThat(expectMsgClass(Verdict::class.java), equalTo((Verdict.GOOD)))
                assertThat(expectMsgClass(Verdict::class.java), equalTo((Verdict.GOOD)))
            }
        }}
    }

    private fun createAd(userId: Int): Ad {
        return Ad(userId, "ad title", "description")
    }
}