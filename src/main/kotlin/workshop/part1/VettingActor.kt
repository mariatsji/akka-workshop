package workshop.part1

import akka.actor.ActorRef
import akka.actor.UntypedActor
import scala.concurrent.duration.FiniteDuration
import workshop.common.ad.Ad
import workshop.common.fraudwordsservice.FraudWord
import workshop.common.fraudwordsservice.FraudWordService
import workshop.common.userservice.UserCriminalRecord
import workshop.common.userservice.UserService

class VettingActor(private val userService: UserService,
                   private val fraudWordService: FraudWordService,
                   private val numVettedAdsActor: ActorRef,
                   private val numVettedAdsInterval: FiniteDuration) : UntypedActor() {

    private var numVettedAds: Int = 0

    override fun preStart() {
        scheduleReportNumVettedAds()
    }

    override fun onReceive(msg: Any?) = when (msg) {
        is Ad -> {
            val verdict = performVetting(msg)
            numVettedAds += 1
            sender().tell(verdict, self())
        }
        is GetNumVettedAds -> sender().tell(NumVettedAds(numVettedAds), self())
        is ReportNumVettedAds -> {
            numVettedAdsActor.tell(NumVettedAds(numVettedAds), self())
            scheduleReportNumVettedAds()
        }
        else -> unhandled(msg)
    }

    private fun performVetting(ad: Ad): Verdict {
        val record = userService.vettUser(ad.userId)
        val fraudWords = fraudWordService.examineWords(ad.toAdWords())

        return toVerdictStatus(record, fraudWords)
    }

    private fun toVerdictStatus(record: UserCriminalRecord, fraudWords: List<FraudWord>): Verdict {
        return if (record == UserCriminalRecord.GOOD && fraudWords.isEmpty()) {
            Verdict.GOOD
        } else {
            Verdict.BAD
        }
    }

    private fun scheduleReportNumVettedAds() {
        context().system().scheduler().scheduleOnce(numVettedAdsInterval, self(),
                ReportNumVettedAds(), context().system().dispatcher(), self())
    }

    internal class GetNumVettedAds

    class NumVettedAds(val numVettedAds: Int?)

    internal class ReportNumVettedAds
}
