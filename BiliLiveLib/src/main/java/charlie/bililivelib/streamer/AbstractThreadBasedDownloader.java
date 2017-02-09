package charlie.bililivelib.streamer;

import charlie.bililivelib.I18n;
import charlie.bililivelib.streamer.event.DownloadEvent;

public abstract class AbstractThreadBasedDownloader extends AbstractDownloader implements Runnable {
    private Thread thread;

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException 当本下载器状态非停止或错误时抛出
     */
    @Override
    public void start() {
        if (status != Status.STOPPED && status != Status.ERROR)
            throw new IllegalStateException("status != STOPPED || ERROR");
        thread = new Thread(this);
        thread.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tryStop() {
        status = Status.STOPPING;
        thread.interrupt();
        fireDownloadEvent(I18n.getString("msg.try_stopping"), DownloadEvent.Kind.STOPPED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("deprecation")
    public void forceStop() {
        thread.stop();
        status = Status.STOPPED;
        fireDownloadEvent(I18n.getString("msg.force_stopped"), DownloadEvent.Kind.STOPPED);
    }

    @Override
    public void run() {
        startupThread();
        download();
    }

    private void startupThread() {
        Thread.currentThread().setName(this.getClass().getSimpleName() + "-" + room.getRoomID());
    }

    protected abstract void download();
}
