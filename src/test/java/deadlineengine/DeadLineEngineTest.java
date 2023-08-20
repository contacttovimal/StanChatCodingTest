package deadlineengine;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class DeadLineEngineTest {

    DeadlineEngine deadlineEngine;
    private Logger logger;

    @Before
    public void setup() {
        deadlineEngine = new DeadlineEngineImpl();
        logger = Configurator.initialize("DeadLineEngineTest", "log4j2.xml").getLogger("DeadLineEngineTest");
    }

    /**
     * Create 10000 Random request in parallel and check if duplicated requestID created
     */
    @Test
    public void checkDuplicateRequestId() {
        Set<Long> requestKeys = ConcurrentHashMap.newKeySet();
        Random random = new Random(10000);
        IntStream.range(0, 100000).parallel().forEach(num -> {
            long deadLineMs = random.nextInt(10000);
            long requestId = deadlineEngine.schedule(deadLineMs);
            if (requestId != 0) {
                requestKeys.add(requestId);
            } else {
                logger.info("error: {}, deadLine:{}" , requestId , deadLineMs);
            }

        });
        Assert.assertEquals(100000, requestKeys.size());
        deadlineEngine.schedule(-100);
        Assert.assertEquals(100000, requestKeys.size());
    }

    /**
     * Create 10 Request with different expiry and poll All Expired one with MAX capacity
     */
    @Test
    public void checkExpiryWithInLimit() throws InterruptedException {
        Set<Long> requestKeys = new HashSet<>();
        long expiryMs = 2000;
        for (int i = 0; i < 10; i++) {
            long requestId = deadlineEngine.schedule(expiryMs);
            requestKeys.add(requestId);
            expiryMs += 2000;
        }
        ;
        Thread.currentThread().sleep(4000);

        Set<Long> expiredRequestList = new HashSet<>();

        deadlineEngine.poll(System.currentTimeMillis(), new Consumer<Long>() {
            @Override
            public void accept(Long aLong) {
                logger.info("expired : {}" , aLong);
                expiredRequestList.add(aLong);
            }
        }, 10);

        Assert.assertEquals(2, expiredRequestList.size());

    }

    /**
     * Create 10 Request with different expiry and poll All Expired one with limited capacity
     * first time poll with max poll capacity 2 should return 2 object even 3 has been expired (6 sec sleep)
     * second poll return one more expired request with max poll capacity 2
     */
    @Test
    public void checkExpiryOutSideLimit() throws InterruptedException {
        Set<Long> requestKeys = new HashSet<>();
        long expiryMs = 2000;
        for (int i = 0; i < 10; i++) {
            long requestId = deadlineEngine.schedule(expiryMs);
            requestKeys.add(requestId);
            expiryMs += 2000;
        }

        Assert.assertEquals(10, requestKeys.size());

        logger.info("Added 10 Object with 2 second incremental expiry offset and sleep for 6 sec");

        Thread.currentThread().sleep(6000);

        Set<Long> expiredRequestList = new HashSet<>();

        deadlineEngine.poll(System.currentTimeMillis(), new Consumer<Long>() {
            @Override
            public void accept(Long aLong) {
                logger.info("callback : expired : {}",  aLong);
                expiredRequestList.add(aLong);
            }
        }, 2);

        Assert.assertEquals(2, expiredRequestList.size());

        deadlineEngine.poll(System.currentTimeMillis(), new Consumer<Long>() {
            @Override
            public void accept(Long aLong) {
                logger.info("expired :{} " , Util.preferredDateString(aLong));
                expiredRequestList.add(aLong);
            }
        }, 2);

        Assert.assertEquals(3, expiredRequestList.size());
    }

    /**
     * Create 10 Request with different expiry and poll All Expired one with MAX capacity
     */
    @Test
    public void checkExpiryAndCancelEvent() throws InterruptedException {
        Set<Long> requestKeys = new HashSet<>();
        long expiryMs = 2000;
        long cancelRequest=0;
        for (int i = 0; i < 10; i++) {
            long requestId = deadlineEngine.schedule(expiryMs);
            cancelRequest = requestId;
            requestKeys.add(requestId);
            expiryMs += 2000;
        }
        Assert.assertEquals(true,deadlineEngine.cancel(cancelRequest));

        Thread.currentThread().sleep(4000);

        Set<Long> expiredRequestList = new HashSet<>();

        deadlineEngine.poll(System.currentTimeMillis(), new Consumer<Long>() {
            @Override
            public void accept(Long aLong) {
                logger.info("expired : {}" ,Util.preferredDateString(aLong));
                expiredRequestList.add(aLong);
            }
        }, 10);

        Assert.assertEquals(2, expiredRequestList.size());
        Assert.assertEquals(7, deadlineEngine.size());
    }

}







