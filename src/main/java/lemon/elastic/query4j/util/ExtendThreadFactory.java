package lemon.elastic.query4j.util;

import java.util.concurrent.ThreadFactory;

public class ExtendThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        return null;
    }

}
