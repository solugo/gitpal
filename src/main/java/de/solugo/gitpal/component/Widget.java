package de.solugo.gitpal.component;

import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 *
 * @author Frederic
 */
public abstract class Widget {

    private final String fxml;
    private Parent root;

    public Widget() {
        this.fxml = String.format("/fxml/%1$s.fxml", this.getClass().getSimpleName());
    }

    public Widget(final String fxml) {
        this.fxml = fxml;
    }

    public Parent getRoot() {
        if (this.root == null) {
            try {
                final URL url = this.getClass().getResource(fxml);
                final FXMLLoader loader = new FXMLLoader(url);
                loader.setController(this);
                loader.load();
                this.root = loader.getRoot();
            } catch (final IOException exception) {
                throw new RuntimeException(exception);
            }
        }
        return root;
    }

}
