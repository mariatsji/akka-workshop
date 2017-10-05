package workshop.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import akka.dispatch.ExecutionContexts;
import scala.concurrent.Future;

public class Utils {

    /**
     * The Akka HTTP examples from the documentation accepts java futures, but akka actors return scala futures.
     * This function creates a java CompletableFuture from a scala future
     *
     * @param scalaFuture The scala future
     * @param <T>         the wrapped value
     * @return a java future
     */
    public static <T> CompletionStage<T> toJavaFuture(Future<T> scalaFuture) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();

        scalaFuture.onComplete(tryVal -> tryVal
                .fold(completableFuture::completeExceptionally,
                        completableFuture::complete), ExecutionContexts.global());

        return completableFuture;
    }


}
