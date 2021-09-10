package com.linkedin.batch;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

public class CustomRetryListener implements RetryListener {

    @Override
    public <T, E extends Throwable> boolean open(RetryContext retryContext, RetryCallback<T, E> retryCallback) {
        if (retryContext.getRetryCount() > 0) {
            System.out.println("Attempting retry");
        }
        return true;
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext retryContext, RetryCallback<T, E> retryCallback, Throwable throwable) {

    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext retryContext, RetryCallback<T, E> retryCallback, Throwable throwable) {
        if (retryContext.getRetryCount() > 0) {
            System.out.println("Failure occurred requiring a retry");
        }
    }
}
