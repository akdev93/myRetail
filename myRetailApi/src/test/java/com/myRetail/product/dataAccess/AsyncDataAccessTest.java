package com.myRetail.product.dataAccess;

import com.myRetail.product.dataAccess.AsyncDataAccess;
import com.myRetail.product.model.AppError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class AsyncDataAccessTest {

    @Before
    public void setUp(){

    }

    @After
    public void tearDown() {

    }

    @org.junit.Test
    public void testAsyncDataFetchWithAdequateThreads() throws Exception {

        TestAsyncDataAccess tada = new TestAsyncDataAccess(3000);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<Optional<String>> futureS1 = tada.getDataAsync(new Object[]{"1"}, pool);
        Future<Optional<String>> futureS2 = tada.getDataAsync(new Object[]{"2"}, pool);
        long t1 = System.currentTimeMillis();
        Optional<String> optS1  = futureS1.get();
        Optional<String> optS2 = futureS2.get();
        long t2 = System.currentTimeMillis();

        System.out.println("Time of execution "+(t2-t1));
        org.junit.Assert.assertTrue(String.format("Took longer than expected Expected (%s) but found (%s)",tada.getExecTime(),(t2-t1)),
                (t2-t1) < (2*tada.getExecTime()));

        org.junit.Assert.assertTrue(String.format("Unexpected message (%s). Expected 'Test Message 1'",optS1.get()),"Test Message 1".equals(optS1.get()));

        org.junit.Assert.assertTrue(String.format("Unexpected message (%s). Expected 'Test Message 2'",optS2.get()),"Test Message 2".equals(optS2.get()));


    }

    @org.junit.Test
    public void testAsyncDataFetchWithInAdequateThreads() throws Exception {

        TestAsyncDataAccess tada = new TestAsyncDataAccess(1000);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<Optional<String>> futureS1 = tada.getDataAsync(new Object[]{"1"}, pool);
        Future<Optional<String>> futureS2 = tada.getDataAsync(new Object[]{"2"}, pool);
        Future<Optional<String>> futureS3 = tada.getDataAsync(new Object[]{"3"}, pool);

        long t1 = System.currentTimeMillis();
        Optional<String> optS1  = futureS1.get();
        Optional<String> optS2 = futureS2.get();
        Optional<String> optS3 = futureS3.get();

        long t2 = System.currentTimeMillis();

        System.out.println("Time of execution "+(t2-t1));
        org.junit.Assert.assertTrue(String.format("Took less than amount of time. Expected (%s) but found (%s)",tada.getExecTime(),(t2-t1)),
                (t2-t1) >= (2*tada.getExecTime()));

        org.junit.Assert.assertTrue(String.format("Took less than amount of time. Expected (%s) but found (%s)",tada.getExecTime(),(t2-t1)),
                (t2-t1) <= (3*tada.getExecTime()));

        org.junit.Assert.assertTrue(String.format("Unexpected message (%s). Expected 'Test Message 1'",optS1.get()),"Test Message 1".equals(optS1.get()));

        org.junit.Assert.assertTrue(String.format("Unexpected message (%s). Expected 'Test Message 2'",optS2.get()),"Test Message 2".equals(optS2.get()));

        org.junit.Assert.assertTrue(String.format("Unexpected message (%s). Expected 'Test Message 3'",optS3.get()),"Test Message 3".equals(optS3.get()));


    }


    @org.junit.Test
    public void testAsyncDataFetchWithException() throws Exception {
        TestAsyncDataAccess tada = new TestAsyncDataAccess(1000,true);
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Future<Optional<String>> futureS1 = null;
        try {
            futureS1 = tada.getDataAsync(new Object[]{"1345"}, pool);
            futureS1.get();
        }
        catch(ExecutionException ee) {
            org.junit.Assert.assertTrue("Unexpected Causative exception ", ee.getCause() instanceof AppError);
        }

    }

    class TestAsyncDataAccess extends AsyncDataAccess<String> {


        private  int execTime=2000;
        private boolean throwException = false;

        public TestAsyncDataAccess(int execTime) {
            this.execTime = execTime;
        }

        public TestAsyncDataAccess(int execTime, boolean throwException) {
            this(execTime);
            this.throwException = throwException;
        }

        public TestAsyncDataAccess() {};

        public Optional<String> getData(Object[] args) {

            try {
                Thread.sleep(execTime);
            }catch(Exception e) {

            }
            if(throwException) {
                throw new AppError("This is for testing");
            }
            return Optional.of(String.format("Test Message %s",args[0].toString()));
        }

        public Future<Optional<String>> getDataAsync(Object[] args, ExecutorService pool) {
            return super.getDataAsync(args,pool);
        }

        public int getExecTime() {
            return this.execTime;
        }
    }
}
