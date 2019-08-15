package com.artezio.javax.jpa.abac.hibernate;

import org.hibernate.EmptyInterceptor;

import javax.validation.constraints.NotNull;
import java.util.Iterator;

public class PostFlushInterceptor extends EmptyInterceptor {
    private Runnable postFlushCallback;

    public void onPostFlush(@NotNull Runnable postFlushListener) {
        this.postFlushCallback = postFlushListener;
    }

    @Override
    public void postFlush(Iterator entities) {
        postFlushCallback.run();
    }

}
