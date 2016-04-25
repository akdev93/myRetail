package com.myRetail.product.dataAccess;


import org.apache.logging.log4j.ThreadContext;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


/**
 * <p>
 * <code>AsyncDataAccess</code> is an abstraction for any data access component to implement asynchronous
 * data lookup. The class implements the functionality to execute the data access asynchronsously using the
 * generic methods - <code>getData(Object[] o)</code> and <code>getDataAsync(Object o, ExecutorService e)</code>.
 * The latter invokes the former using a call object.
 * </p>
 * <p>
 *     Subclasses will need to create the more type enforced APIs that delegate the calls to
 *     <code>getDataAsync</code> for async data access.
 * </p>
 * @param <T>
 */
public abstract class AsyncDataAccess<T> {

    /**
     * <p>
     * Subclasses impleent the interaction with the data store or service to fetch the data.
     * </p>
     * @param args
     * @return
     */
    protected abstract Optional<T> getData(Object[] args);

    /**
     * <p>
     * Calls <code>getData(Object[] args)</code> asynchrnously using a call object and a provided thread
     * poool.
     * </p>
     * @param args arguments for data access
     * @param pool thread pool
     * @return future
     */
    protected Future<Optional<T>> getDataAsync(Object[] args, ExecutorService pool) {
        AsyncCall call = new AsyncCall(args, ThreadContext.get("requestId"));
        return pool.submit(call);
    }


    /**
     * <p>
     * <code>AsyncCall</code> implements <code>Callable</code> and is used to call <code>getData(Object[] o)</code>
     * asynchrnously using a thread pool.
     * </p>
     */
    private class AsyncCall implements Callable<Optional<T>> {

        private Object[] args;
        private String requestId;
        public AsyncCall(Object[]args) {
            this(args,null);
        }
        public AsyncCall(Object[] args, String requestId) {
            this.args = args;
            this.requestId = requestId;
        }


        /**
         * <p>
         *     Calls <code>getData(Object[] args)</code> in a thread managed by a thread pool.
         * </p>
         * @return
         */
        public Optional<T> call() {
            ThreadContext.put("requestId", requestId);
            return getData(args);
        }
    }
}

