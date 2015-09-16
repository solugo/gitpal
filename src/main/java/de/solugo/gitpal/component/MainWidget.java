package de.solugo.gitpal.component;

import de.solugo.gitpal.context.TabContext;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;

/**
 *
 * @author Frederic Kneier
 */
public class MainWidget extends Widget {

    private TabContext tabContext;

    @FXML
    private TabPane tabPane;
    
    @FXML
    private MenuItem refreshMenuItem;

    @FXML
    private void initialize() {
        this.tabContext = new TabContext(tabPane);
    }

    public TabContext getTabContext() {
        return tabContext;
    }

}
