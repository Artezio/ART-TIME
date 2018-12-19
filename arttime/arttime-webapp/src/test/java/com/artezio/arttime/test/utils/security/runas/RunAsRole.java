package com.artezio.arttime.test.utils.security.runas;

import java.util.concurrent.Callable;

class RunAsRole {
    public <V> V call(Callable<V> callable) throws Exception {
        return callable.call();
    }
}
