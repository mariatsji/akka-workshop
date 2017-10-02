package workshop.common;

import java.util.concurrent.CompletableFuture;

import akka.dispatch.ExecutionContexts;
import akka.japi.JavaPartialFunction;
import scala.concurrent.Future;

public class Utils {

    /**
     * The Akka HTTP examples from the documentation accepts java futures, but akka actors return scala futures.
     * This function creates a java CompletableFuture from a scala future
     * @param scalaFuture The scala future
     * @param <T> the wrapped value
     * @return a java future
     */
    public static <T> CompletableFuture<T> toJavaFuture(Future<T> scalaFuture) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();

        // happy path
        scalaFuture.onSuccess(new JavaPartialFunction<T, Object>() {
            @Override
            public Object apply(T x, boolean isCheck) throws Exception {
                completableFuture.complete(x);
                return x;
            }
        }, ExecutionContexts.global());

        // sad path
        scalaFuture.onFailure(new JavaPartialFunction<Throwable, Object>() {
            @Override
            public Object apply(Throwable x, boolean isCheck) throws Exception {
                completableFuture.completeExceptionally(x);
                return x;
            }

        }, ExecutionContexts.global());

        return completableFuture;
    }


}
