package rider.aptener.com.riderlivedrive.timers;

public class StopWatch {

    private long startTime = 0;
    private long stopTime = 0;
    private long pauseStart = 0;
    private long pauseEnd = 0;
    private boolean running = false;
    private long curTime = 0;
    private long pauseDiff = 0;

    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }

    public void pause() {
        this.pauseStart = curTime;
        pauseDiff = 0;
    }

    public void resume() {
        this.pauseEnd = System.currentTimeMillis();
        pauseDiff = (pauseEnd - pauseStart);
    }

    // elaspsed time in seconds
    public long getElapsedTimeSecs() {
        long elapsed;
        if (running) {
            elapsed = (curTime - startTime);

            elapsed = (elapsed / 1000) % 60;
        } else {
            elapsed = ((stopTime - startTime) / 1000);
        }
        return elapsed;
    }

    public long getElapsedTimeMin() {
        long elapsed = 0;
        if (running) {
            elapsed = (curTime - startTime);

            elapsed = ((elapsed / 1000) / 60) % 60;
        }
        return elapsed;
    }

    // elaspsed time in hours
    public long getElapsedTimeHour() {
        long elapsed = 0;
        if (running) {
            curTime = System.currentTimeMillis();
            if (pauseDiff > 0) {
                curTime = curTime - pauseDiff;
            }
            elapsed = (curTime - startTime);

            elapsed = (((elapsed / 1000) / 60) / 60);
        }
        return elapsed;
    }

}