package deadlineengine;

import lombok.*;

import java.util.Date;

@AllArgsConstructor()
@Builder
@Data
public class DeadLineRequest {

    @lombok.NonNull
    private long requestId;

    @lombok.NonNull
    private long offSetMs;

    @lombok.NonNull
    private long initMs;

    public long getExpiryMs(){ return (initMs + offSetMs);}

    @Override
    public String toString() {
        return "DeadLineRequest{" +
                "requestId=" + requestId +
                ", offSetMs=" + offSetMs +
                ", initMs=" + initMs +
                ", expiryTime=" + Util.preferredDateString(getExpiryMs()) +
                '}';
    }
}
