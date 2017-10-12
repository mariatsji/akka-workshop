package workshop.part1

import akka.actor.ActorRef
import akka.actor.UntypedActor
import scala.concurrent.duration.FiniteDuration
import workshop.common.fraudwordsservice.FraudWordService
import workshop.common.userservice.UserService

class VettingActor(private val userService: UserService,
                   private val fraudWordService: FraudWordService,
                   private val numVettedAdsActor: ActorRef,
                   private val numVettedAdsInterval: FiniteDuration) : UntypedActor() {

    override fun onReceive(msg: Any?) {}

    internal class GetNumVettedAds

    class NumVettedAds(val numVettedAds: Int?)

    internal class ReportNumVettedAds
}
