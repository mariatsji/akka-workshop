package workshop.part2;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import javaslang.collection.List;
import workshop.common.fraudwordsservice.FraudWord;
import workshop.common.fraudwordsservice.FraudWordService;

public class FraudWordActor extends AbstractActor {

    private final FraudWordService fraudWordService;

    public FraudWordActor(FraudWordService fraudWordService) {
        this.fraudWordService = fraudWordService;
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
            .match(ExamineWords.class, m -> {
                sender().tell(new ExamineWordsResult(fraudWordService.examineWords(m.words)), sender());
            })
            .build();
    }

    static class ExamineWords {
        final List<String> words;

        public ExamineWords(List<String> words) {
            this.words = words;
        }
    }

    static class ExamineWordsResult {
        final List<FraudWord> fraudWords;

        public ExamineWordsResult(List<FraudWord> fraudWords) {
            this.fraudWords = fraudWords;
        }
    }
}
