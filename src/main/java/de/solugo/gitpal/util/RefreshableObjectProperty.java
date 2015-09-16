package de.solugo.gitpal.util;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;

/**
 *
 * @author Frederic Kneier
 */
public class RefreshableObjectProperty<T> extends ReadOnlyObjectProperty<T>{
    private final RefreshableReadOnlyObjectWrapper<T> wrapper;

    public RefreshableObjectProperty(final RefreshableReadOnlyObjectWrapper<T> wrapper) {
        this.wrapper = wrapper;
    }

    public void refresh() {
        wrapper.refresh();
    }
    
    @Override
    public T get() {
        return wrapper.get();
    }

    @Override
    public void addListener(final ChangeListener<? super T> listener) {
        wrapper.addListener(listener);
    }

    @Override
    public void removeListener(final ChangeListener<? super T> listener) {
        wrapper.removeListener(listener);
    }

    @Override
    public void addListener(final InvalidationListener listener) {
        wrapper.addListener(listener);
    }

    @Override
    public void removeListener(final InvalidationListener listener) {
        wrapper.removeListener(listener);
    }

    @Override
    public Object getBean() {
        return wrapper.getBean();
    }

    @Override
    public String getName() {
        return wrapper.getName();
    }
    
}
