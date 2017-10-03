package workshop.part4;

import java.util.concurrent.atomic.AtomicInteger;

public final class IDProvider {

    private static final AtomicInteger counter = new AtomicInteger(0);

    public static String nextId() {
        return String.valueOf(getCounter());
    }

    private static Integer getCounter() {
        return counter.incrementAndGet();
    }


}
