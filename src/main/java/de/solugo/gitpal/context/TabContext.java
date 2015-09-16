package de.solugo.gitpal.context;

import java.util.Map;
import java.util.WeakHashMap;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 *
 * @author Frederic Kneier
 */
public class TabContext {

    private final Map<String, Tab> tabs = new WeakHashMap<>();
    private final TabPane tabPane;

    public TabContext(final TabPane tabPane) {
        this.tabPane = tabPane;
        this.tabPane.getTabs().addListener(this::onTabsChanged);
    }

    public boolean hasTab(final String name) {
        return getTab(name) != null;
    }

    public Tab addTab(final String name, final Tab tab) {
        this.tabs.put(name, tab);
        this.tabPane.getTabs().add(tab);

        return tab;
    }

    public Tab showTab(final String name) {
        return showTab(getTab(name));
    }

    public Tab showTab(final Tab tab) {
        this.tabPane.getSelectionModel().select(tab);
        return tab;
    }

    public Tab showTab(final String name, final Tab tab) {
        this.tabPane.getSelectionModel().select(this.addTab(name, tab));
        return tab;
    }

    public Tab getTab(final String name) {
        return tabs.get(name);
    }

    public void removeTab(final String name) {
        removeTab(this.tabs.get(name));
    }

    public void removeTab(final Tab tab) {
        this.tabPane.getTabs().remove(tab);
    }

    public void onTabsChanged(final ListChangeListener.Change<? extends Tab> change) {
        while (change.next()) {
            if (change.wasRemoved()) {
                change.getRemoved().stream().forEach((tab) -> {
                    this.tabs.values().remove(tab);
                });
            }
        }
    }
}
