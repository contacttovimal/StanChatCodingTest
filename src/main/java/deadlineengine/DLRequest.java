package deadlineengine;

import lombok.*;

@AllArgsConstructor()
@Builder
@Data
public class DLRequest {

    @lombok.NonNull
    private long requestId;

    @lombok.NonNull
    private long offSetMs;

    @lombok.NonNull
    private long initMs;

    public long getExpiryMs(){ return (initMs + offSetMs);}

}
