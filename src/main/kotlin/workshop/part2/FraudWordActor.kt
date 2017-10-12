package workshop.part2

import akka.actor.UntypedActor
import workshop.common.fraudwordsservice.FraudWord
import workshop.common.fraudwordsservice.FraudWordService

class FraudWordActor(private val fraudWordService: FraudWordService) : UntypedActor() {
    override fun onReceive(message: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class ExamineWords(val words: List<String>)

    class ExamineWordsResult(val fraudWords: List<FraudWord>)
}
