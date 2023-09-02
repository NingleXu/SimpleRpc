package com.gdou.common.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ningle
 * @version : SequenceIDGenerator.java, v 0.1 2023/08/31 09:25 ningle
 **/
public class SequenceIDGenerator {
    private static final AtomicInteger id = new AtomicInteger();

    public static int nextId() {
        return id.incrementAndGet();
    }
}
