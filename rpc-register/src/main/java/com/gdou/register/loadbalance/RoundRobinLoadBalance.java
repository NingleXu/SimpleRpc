package com.gdou.register.loadbalance;

import java.util.List;


/**
 * @author ningle
 * @version : RoundRobinLoadBalance.java, v 0.1 2023/09/01 00:21 ningle
 **/
public class RoundRobinLoadBalance<T> extends AbstractLoadBalance {

    @Override
    protected <T> T doSelect(List<T> instanstList) {
        throw new UnsupportedOperationException("暂不支持轮询负载均衡策略");
    }

}
