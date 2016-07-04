/**
 * Created by leopold on 2016/7/5.
 */
public class GoogleSearchTime {
    long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time1, long time2) {
        this.time = time2-time1;
    }
}
