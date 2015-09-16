package de.solugo.gitpal.util;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Frederic Kneier
 */
public class RefreshableReadOnlyObjectWrapper<T> extends ReadOnlyObjectWrapper<T> {

    private final LazyProvider<T> supplier;

    public RefreshableReadOnlyObjectWrapper(final LazyProvider<T> supplier) {
        super(Util.call(() -> supplier.get()));
        this.supplier = supplier;
    }

    public void refresh() {
        this.set(Util.call(() -> supplier.get()));
    }

    public RefreshableReadOnlyObjectWrapper<T> dependingOn(final ObservableValue... values) {
        for (final ObservableValue value : values) {
            value.addListener((e) -> this.set(null));
        }
        return this;
    }

    public static interface LazyProvider<T> {

        public T get() throws Exception;
    }

}
