package cache;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * One Supplier Call per key to generate value
 * Null Key & Null value is not allowed
 * Supplier Function is responsible to generate value by Key - User has to provide his own supplier function as per the requirement
 *
 * */
public final class CacheImpl<K,V> implements Cache<K,V>, Cloneable{

    private Logger logger;

    private Map<K,V> dataMap = new ConcurrentHashMap<>(); //can be replaced with ConcurrentHashMap with ComputeIfAbsent()
    private volatile Function<K,V> supplier;

    private CacheImpl(){}

    public CacheImpl(Function<K,V> supplier){
        if(supplier == null){
            throw new RuntimeException("Supplier can not be null.");
        }
        this.supplier = supplier;
        logger = Configurator.initialize("CacheImpl", "log4j2.xml").getLogger("CacheImpl");
        logger.info("CacheImpl has been Successfully Initialized. cacheSize: {}", dataMap.size());
    }

    private V getOrCompute(K key){
        if(!validateKey(key)){
            throw new RuntimeException("Null key not supported.");
        }
        V value = dataMap.get(key);
        if(value != null){
            return value;
        }
        if(this.supplier != null){
            synchronized (supplier) {
                if(!dataMap.containsKey(key)) {
                    V newValue = supplier.apply(key);
                    if(newValue==null){
                        logger.info("Null value has been ignored for key: {}, size :{}",key,dataMap.size());
                        return null;
                    };
                    dataMap.put(key, newValue);
                    logger.info(" key: {} , value:{}, size:{}",key,newValue,dataMap.size());
                }
            }
            return dataMap.get(key);
        }
        throw new RuntimeException("Supplier is null");

    }

    private boolean validateKey(K key){
        return key!=null;
    }

    @Override
    public V get(K key) {
        return getOrCompute(key);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone not supported!");
    }
}
