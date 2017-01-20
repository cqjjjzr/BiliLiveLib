package charlie.bililivelib.user;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;

/**
 * {@link com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController}
 * This {@link AjaxController} resynchronizes calls calling from the main thread.
 * The idea is that asynchronous AJAX calls performed directly in response to a user
 * action (therefore in the "main" thread and not in the thread of a background task)
 * are directly useful for the user. To easily have a testable state, these calls
 * are performed synchronously.
 * <p>
 * <red>Why we add this in the project? Because the NicelyResynchronizingAjaxController will write fucking logs!</red>
 *
 * @author Marc Guillemot
 */
public class ResynchronizingAjaxController extends AjaxController {
    private transient WeakReference<Thread> originatedThreadRef;

    /**
     * Creates an instance.
     */
    public ResynchronizingAjaxController() {
        init();
    }

    /**
     * Initializes this instance.
     */
    private void init() {
        originatedThreadRef = new WeakReference<>(Thread.currentThread());
    }

    /**
     * Resynchronizes calls performed from the thread where this instance has been created.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean processSynchron(final HtmlPage page, final WebRequest settings, final boolean async) {
        return !async || isInOriginalThread();
    }

    /**
     * Indicates if the currently executing thread is the one in which this instance has been created.
     *
     * @return {@code true} if it's the same thread
     */
    boolean isInOriginalThread() {
        return Thread.currentThread() == originatedThreadRef.get();
    }

    /**
     * Custom deserialization logic.
     *
     * @param stream the stream from which to read the object
     * @throws IOException            if an IO error occurs
     * @throws ClassNotFoundException if a class cannot be found
     */
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        init();
    }

}