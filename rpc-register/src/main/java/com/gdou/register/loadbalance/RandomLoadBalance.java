package com.gdou.register.loadbalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author ningle
 * @version : RandomLoadBalance.java, v 0.1 2023/09/01 00:19 ningle
 **/
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected <T> T doSelect(List<T> instanstList) {
        int length = instanstList.size();
        return instanstList.get(ThreadLocalRandom.current().nextInt(length));
    }
}
