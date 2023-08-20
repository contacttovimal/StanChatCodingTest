package deadlineengine;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Util {

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss.sss");


    private static final int RANDOM_BITS = 22;
    private static final int RANDOM_MASK = 0x003fffff;
    private static final long TSID_EPOCH = Instant.parse("2023-01-01T00:00:00.000Z").toEpochMilli();
    private static final AtomicInteger counter = new AtomicInteger((new SecureRandom()).nextInt());

    public static long nextRequestId(){
        final long time = (System.currentTimeMillis() - TSID_EPOCH) << RANDOM_BITS;
        final long tail = counter.incrementAndGet() & RANDOM_MASK;
        return (time | tail);
    }

    public static String preferredDateString(long millis){
       return dateFormat.format(new Date(millis));
    }
}
