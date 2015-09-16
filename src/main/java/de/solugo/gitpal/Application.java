package de.solugo.gitpal;

import de.solugo.gitpal.component.MainWidget;
import de.solugo.gitpal.component.RepositoryListWidget;
import de.solugo.gitpal.util.ExtendedURLStreamHandlerFactory;
import java.net.URL;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Frederic
 */
public class Application extends javafx.application.Application {

    public static void main(final String[] arguments) {
        URL.setURLStreamHandlerFactory(new ExtendedURLStreamHandlerFactory());
        launch(arguments);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        final MainWidget mainWidget = new MainWidget();

        stage.setTitle("GIT GUI");
        stage.setWidth(800);
        stage.setHeight(600);
        stage.show();
        stage.getIcons().add(new Image("icon:git.svg?size=64"));
        stage.setScene(new Scene(mainWidget.getRoot()));

        final Tab tab = mainWidget.getTabContext().showTab("root", new Tab("Pepositories"));
        tab.setClosable(false);
        tab.setContent(new RepositoryListWidget(mainWidget.getTabContext()).getRoot());
    }

}
