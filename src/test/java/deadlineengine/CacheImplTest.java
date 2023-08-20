package deadlineengine;

import cache.Cache;
import cache.CacheImpl;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.stream.IntStream;

public class CacheImplTest {

    private Logger logger;
    //Dummy Supplier has been written for testing
    //User has to inject his own value generation function
    private static Function<String,String> supplier = Mockito.spy(new Function<String, String>() {
        @Override
        public String apply(String input) {
            if(input!=null && input.contains("-")) {
                StringBuilder stringBuilder = new StringBuilder();
                return stringBuilder.append("value-").append(input.split("-")[1]).toString();
            }
            return null;
        }
    });

    @Before
    public void setup() {
        logger = Configurator.initialize("CacheImplTest", "log4j2.xml").getLogger("CacheImplTest");
    }

    @Test
    public void verifyMethodCall(){
        Mockito.clearInvocations(supplier);
        Cache<String,String> cacheImpl = Mockito.spy(new CacheImpl<String,String>(supplier));

        IntStream.range(0,100).parallel().forEach(input->{
            cacheImpl.get("key-"+input);
        });
        Assert.assertEquals("value-100",cacheImpl.get("key-100"));
        logger.info("verify cache generation size {}",100);
        Mockito.verify(cacheImpl,Mockito.times(101)).get(Mockito.any());
        IntStream.range(0,100).parallel().forEach(input->{
            cacheImpl.get("key-"+input);
        });
        Mockito.verify(cacheImpl,Mockito.times(201)).get(Mockito.any());
        Mockito.verify(supplier,Mockito.times(101)).apply(Mockito.any());
        logger.info("verify method call -  get:{}times ,supplier:{} times",201,101);
    }

    @Test
    public void verifyMultiThreadedCall() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Mockito.clearInvocations(supplier);
            Cache<String, String> cacheImpl = Mockito.spy(new CacheImpl<String, String>(supplier));
            List<Callable<Boolean>> taskList = new ArrayList<>();
            taskList.add(new Callable<Boolean>()  {
                @Override
                public Boolean call() throws Exception {
                    IntStream.range(0, 100).parallel().forEach(input -> {
                        cacheImpl.get("key-" + input);
                    });
                    logger.info("100 key generation completed");
                    return true;
                }
            });

            taskList.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    IntStream.range(0, 200).parallel().forEach(input -> {
                        cacheImpl.get("key-" + input);
                    });
                    logger.info("200 key generation completed");
                    return true;
                }
            });
            executorService.invokeAll(taskList);

            logger.info("main thread join with timeout:{}", 3000);

            Mockito.verify(cacheImpl, Mockito.times(300)).get(Mockito.any());
            Mockito.verify(supplier, Mockito.times(200)).apply(Mockito.any());
            logger.info("verify method call : get:{}times , supplier:{} times", 300, 200);


        }catch(InterruptedException ie){
            logger.error(ie);
        }finally {
            executorService.shutdown();
        }
    }

    @Test(expected = RuntimeException.class)
    public void checkNullKey(){
        Mockito.clearInvocations(supplier);
        Cache<String,String> cacheImpl = Mockito.spy(new CacheImpl<String,String>(supplier));

        IntStream.range(0,10).parallel().forEach(input->{
            cacheImpl.get("key-"+input);
        });
        Assert.assertEquals("value-10",cacheImpl.get("key-10"));
        Mockito.verify(cacheImpl,Mockito.times(11)).get(Mockito.any());
        Mockito.verify(supplier,Mockito.times(11)).apply(Mockito.any());
        Assert.assertEquals(null,cacheImpl.get(null));
        logger.info("verify method call : get:{}times , supplier:{} times",11,11);
    }
    @Test
    public void checkNullValue(){
        Mockito.clearInvocations(supplier);
        Cache<String,String> cacheImpl = Mockito.spy(new CacheImpl<String,String>(supplier));

        IntStream.range(0,10).parallel().forEach(input->{
            cacheImpl.get("key-"+input);
        });
        Assert.assertEquals("value-10",cacheImpl.get("key-10"));
        Assert.assertEquals(null,cacheImpl.get("InvalidKey"));
        Mockito.verify(cacheImpl,Mockito.times(12)).get(Mockito.any());
        Mockito.verify(supplier,Mockito.times(12)).apply(Mockito.any());
        logger.info("verify method call : get:{}times , supplier:{} times",12,12);
    }
}
