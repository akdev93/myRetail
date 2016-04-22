package com.myRetail.product.dataAccess;


import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public abstract class AsyncDataAccess<T> {

    protected abstract Optional<T> getData(Object[] args);

    protected Future<Optional<T>> getDataAsync(Object[] args, ExecutorService pool) {
        AsyncCall call = new AsyncCall(args);
        return pool.submit(call);
    }


    private class AsyncCall implements Callable<Optional<T>> {

        private Object[] args;
        public AsyncCall(Object[] args) {
            this.args = args;
        }


        public Optional<T> call() {
            return getData(args);
        }
    }
}

