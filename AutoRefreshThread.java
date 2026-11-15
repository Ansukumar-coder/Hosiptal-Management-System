public class AutoRefreshThread extends Thread {

    private final HospitalGUI gui;
    private final int seconds;
    private volatile boolean running = true;

    public AutoRefreshThread(HospitalGUI gui, int seconds) {
        this.gui = gui;
        this.seconds = seconds;
        setDaemon(true);   
    }

    public void stopRefreshing() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(seconds * 1000L);
                gui.refreshPatients();
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
