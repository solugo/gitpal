package de.solugo.gitpal.util;

import java.util.concurrent.Callable;

/**
 *
 * @author Frederic Kneier
 */
public class Util {

    public static <T> T call(final SaveCallable<T> callable) {
        if (callable != null) {
            try {
                return callable.call();
            } catch (final Exception exception) {
                throw new RuntimeException(exception);
            }
        } else {
            return null;
        }
    }

    public static <T> T callUi(final SaveCallable<T> callable) {
        if (callable != null) {
            try {
                return callable.call();
            } catch (final Exception exception) {
                throw new RuntimeException(exception);
            }
        } else {
            return null;
        }
    }

    public static <T> void run(final SaveRunnable runnable) {
        if (runnable != null) {
            try {
                runnable.run();
            } catch (final Exception exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    public static <T> void runUi(final SaveRunnable runnable) {
        if (runnable != null) {
            try {
                runnable.run();
            } catch (final Exception exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    public static <T> T getDefault(final T value, final SaveCallable<T> callable) {
        if (value == null && callable != null) {
            return call(callable);
        } else {
            return value;
        }
    }

    public static <T> void using(final T value, final SaveUsing<T> use) {
        if (use != null) {
            try {
                use.use(value);
            } catch (final Exception exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    public static interface SaveUsing<T> {

        public void use(final T value) throws Exception;
    }

    public static interface SaveCallable<T> {

        public T call() throws Exception;
    }

    public static interface SaveRunnable {

        public void run() throws Exception;
    }
}
