package workshop.part2

import akka.actor.UntypedActor
import workshop.common.fraudwordsservice.FraudWord
import workshop.common.fraudwordsservice.FraudWordService

class FraudWordActor(private val fraudWordService: FraudWordService) : UntypedActor() {

    override fun onReceive(msg: Any?) = when (msg) {
        is ExamineWords -> sender().tell(ExamineWordsResult(fraudWordService.examineWords(msg.words)), sender())
        else -> unhandled(msg)
    }

    class ExamineWords(val words: List<String>)

    class ExamineWordsResult(val fraudWords: List<FraudWord>)
}
