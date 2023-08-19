package deadlineengine;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DeadlineEngineImpl implements DeadlineEngine {

    private PriorityBlockingQueue<DLRequest> dataQueue = new PriorityBlockingQueue<DLRequest>(1000, (current, other) -> {
        return Long.compare(current.getExpiryMs(), other.getExpiryMs());
    });

    private Map<Long, DLRequest> requestMap = new ConcurrentHashMap<>(1000);
    private Logger logger;

    private static final AtomicLong uniqueReqId = new AtomicLong(1000);

    public DeadlineEngineImpl() {
        logger = Configurator.initialize("DeadlineEngineImpl", "log4j2.xml").getLogger("DeadlineEngineImpl");
        logger.info("DeadLineEngine has been Successfully Initialized. first RequestId : {}", TSID.next());
    }

    @Override
    public long schedule(long deadlineMs) {
        long currentMs = System.currentTimeMillis();
        if (deadlineMs < 0) {
            logger.info("Invalid Request deadlineMs:{}, currentMs:{}", deadlineMs, currentMs);
            return 0;
        }
        DLRequest newRequest = new DLRequest(TSID.next(), deadlineMs, currentMs);
        requestMap.put(newRequest.getRequestId(), newRequest);
        dataQueue.add(newRequest);
        return newRequest.getRequestId();
    }

    @Override
    public boolean cancel(long requestId) {
        if (!requestMap.containsKey(requestId)) {
            logger.info("Invalid Request : {}", requestId);
            return false;
        }
        DLRequest request = requestMap.remove(requestId);
        boolean removedFromQueue = dataQueue.remove(request);
        logger.info("removed from requestMap : {} , ExecutedInQueue:{}", request, !removedFromQueue);
        return true;
    }

    @Override
    public int poll(long nowMs, Consumer<Long> handler, int maxPoll) {
        Collection<DLRequest> expiredRequest = new ArrayList<>(maxPoll);
        dataQueue.stream().limit(maxPoll).forEach(dlRequest -> {
            if (dlRequest.getExpiryMs() < nowMs) {
                try {
                    expiredRequest.add(dataQueue.poll(100, TimeUnit.MILLISECONDS));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        expiredRequest.forEach(request -> {
            //logger.info("callback for expired request : {}", request);
            handler.accept(request.getRequestId());
        });
        Set<Long> keysToBeRemoved = expiredRequest.stream().map(request -> request.getRequestId()).collect(Collectors.toSet());
        requestMap.keySet().removeAll(keysToBeRemoved);
        logger.info("removed expired requests from request map keys:{} , size:{}", keysToBeRemoved, requestMap.size());
        return expiredRequest.size();
    }

    @Override
    public int size() {
        return requestMap.size();
    }
}
