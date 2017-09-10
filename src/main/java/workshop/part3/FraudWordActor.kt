package workshop.part3

import akka.actor.UntypedActor
import workshop.common.fraudwordsservice.FraudWord
import workshop.common.fraudwordsservice.FraudWordService

class FraudWordActor(private val fraudWordService: FraudWordService) : UntypedActor() {

    override fun onReceive(msg: Any?) = when (msg) {
        is ExamineWords -> sender().tell(ExamineWordsResult(fraudWordService.examineWords(msg.words)), sender())
        else -> unhandled(msg)
    }

    data class ExamineWords(val words: List<String>)

    data class ExamineWordsResult(val fraudWords: List<FraudWord>)
}
