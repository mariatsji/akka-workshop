package workshop.common.fraudwordsservice

open class FraudWordService {

    open fun examineWords(words: List<String>): List<FraudWord> {
        return words
                .map { it.trim() }
                .filter { !it.isEmpty() }
                .filter { allWords().contains(FraudWord(it)) }
                .map{ FraudWord(it) }
    }

    private fun allWords(): List<FraudWord> {
        return listOf("westernunion", "advance", "nigeria", "wiretransfer").map{ FraudWord(it) }
    }
}
