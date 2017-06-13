package workshop.fraudwordsservice;

import javaslang.collection.List;
import javaslang.control.Either;

public interface FraudWordService {

    Either<Throwable, List<FraudWord>> examineWords(List<String> words);
}
