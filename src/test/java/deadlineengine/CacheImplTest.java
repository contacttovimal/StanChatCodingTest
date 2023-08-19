package deadlineengine;

import cache.Cache;
import cache.CacheImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.function.Function;
import java.util.stream.IntStream;

public class CacheImplTest {


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

    }

    @Test
    public void verifyMethodCall(){
        Mockito.clearInvocations(supplier);
        Cache<String,String> cacheImpl = Mockito.spy(new CacheImpl<String,String>(supplier));

        IntStream.range(0,100).parallel().forEach(input->{
            cacheImpl.get("key-"+input);
        });
        Assert.assertEquals("value-100",cacheImpl.get("key-100"));
        Mockito.verify(cacheImpl,Mockito.times(101)).get(Mockito.any());
        IntStream.range(0,100).parallel().forEach(input->{
            cacheImpl.get("key-"+input);
        });
        Mockito.verify(cacheImpl,Mockito.times(201)).get(Mockito.any());
        Mockito.verify(supplier,Mockito.times(101)).apply(Mockito.any());
    }

    @Test
    public void verifyMultiThreadedCall() throws InterruptedException {
        Mockito.clearInvocations(supplier);
        Cache<String,String> cacheImpl = Mockito.spy(new CacheImpl<String,String>(supplier));

        Thread thread1 = new Thread(()->{
            IntStream.range(0,100).parallel().forEach(input->{
                cacheImpl.get("key-"+input);
            });
        },"Thread-1");

        Thread thread2 = new Thread(()->{
            IntStream.range(0,200).parallel().forEach(input->{
                cacheImpl.get("key-"+input);
            });
        },"Thread-2");

        thread1.start();
        thread2.start();

        Thread.currentThread().join(3000);

        Mockito.verify(cacheImpl,Mockito.times(300)).get(Mockito.any());
        Mockito.verify(supplier,Mockito.times(200)).apply(Mockito.any());
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
    }
}
