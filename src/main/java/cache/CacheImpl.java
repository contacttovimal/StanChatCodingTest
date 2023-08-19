package cache;

import deadlineengine.TSID;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class CacheImpl<K,V> implements Cache<K,V>{

    private Logger logger;

    private Map<K,V> dataMap = new ConcurrentHashMap<>();
    private volatile Function<K,V> supplier;

    private CacheImpl(){}

    public CacheImpl(Function<K,V> supplier){
        this.supplier = supplier;
        logger = Configurator.initialize("CacheImpl", "log4j2.xml").getLogger("CacheImpl");
        logger.info("CacheImpl has been Successfully Initialized. cacheSize: {}", dataMap.size());
    }

    private V getOrCompute(K key){
        if(!validateKey(key)){
            throw new RuntimeException("Invalid Key! Null not supported.");
        }
        if(dataMap.containsKey(key)){
            return dataMap.get(key);
        }
        if(this.supplier != null){
            synchronized (supplier) {
                if(!dataMap.containsKey(key)) {
                    V value = supplier.apply(key);
                    if(value!=null){
                        dataMap.put(key, value);
                        logger.info(" key: {} , value:{}, size:{}",key,value,dataMap.size());
                    };
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




}
