package com.gdou.register.loadbalance;

import java.util.List;

/**
 * @author ningle
 * @version : LoadBalance.java, v 0.1 2023/09/01 00:16 ningle
 **/
public interface LoadBalance {
    <T> T select(List<T> instanstList);
}
