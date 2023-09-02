package com.gdou.register.loadbalance;


import java.util.List;

/**
 * @author ningle
 * @version : AbstractLoadBalance.java, v 0.1 2023/09/01 00:12 ningle
 * <p>
 * 负载均衡的方式
 **/
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public <T> T select(List<T> instanstList) {
        if (instanstList.isEmpty()) {
            return null;
        }
        if (instanstList.size() == 1) {
            return instanstList.get(0);
        }
        return doSelect(instanstList);
    }

    protected abstract <T> T doSelect(List<T> instanstList);

}
