package de.solugo.gitpal.component;

import de.solugo.gitpal.context.TabContext;
import de.solugo.gitpal.util.RefreshableObjectProperty;
import de.solugo.gitpal.util.RepositoryInfo;
import de.solugo.gitpal.util.RepositoryInfoCache;
import java.io.IOException;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Repository;

/**
 *
 * @author Frederic
 */
public class RepositoryListWidget extends Widget {

    private final TabContext tabContext;

    @FXML
    private ListView<RefreshableObjectProperty<RepositoryInfo>> listView;

    @FXML
    private Button refreshButton;

    @FXML
    private Button pullButton;

    @FXML
    private Button pushButton;

    @FXML
    private Button fetchButton;

    public RepositoryListWidget(final TabContext tabContext) {
        this.tabContext = tabContext;
    }

    @FXML
    protected void initialize() {
        this.listView.setItems(FXCollections.observableArrayList((item) -> new Observable[]{item}));
        this.listView.setOnMouseClicked(this::onItemClicked);
        this.listView.setCellFactory(list -> new RepositoryListCell());

        this.refreshButton.setOnAction((e) -> this.refresh());

        this.refresh();
        if (this.listView.getItems().size() == 1) {
            this.showItem(this.listView.getItems().get(0));
        }
    }

    public void refresh() {
        final ObservableList<RefreshableObjectProperty<RepositoryInfo>> items = this.listView.getItems();
        items.clear();
        RepositoryInfoCache.list(".").stream().forEach((repositoryInfo) -> {
            items.add(repositoryInfo);
        });

    }

    public void onItemClicked(final MouseEvent event) {
        if (event.getClickCount() == 2) {
            showItem(this.listView.getSelectionModel().getSelectedItem());
        }
    }

    private void showItem(final RefreshableObjectProperty<RepositoryInfo> repositoryInfoProperty) {
        if (repositoryInfoProperty != null) {
            final String tabTitle = repositoryInfoProperty.get().getName();
            final String tabName = "repository-details:" + tabTitle;
            if (!tabContext.hasTab(tabName)) {
                tabContext.showTab(tabName, new Tab(tabTitle)).setContent(new RepositoryDetailsWidget(tabContext, repositoryInfoProperty).getRoot());
            } else {
                tabContext.showTab(tabName);
            }
        }
    }

    public static class RepositoryListItem {

        private final Repository repository;

        public RepositoryListItem(final Repository repository) {
            this.repository = repository;
        }

        public Repository getRepository() {
            return repository;
        }

    }

    public static class RepositoryListCell extends ListCell<RefreshableObjectProperty<RepositoryInfo>> {

        private final VBox container = new VBox(10);
        private final HBox statusBox = new HBox(10);
        private final ImageView statusIconView = new ImageView();
        private final HBox detailsBox = new HBox();
        private final ImageView branchIconView = new ImageView("icon:branch.svg?size=16&color=0009");

        private final Label statusLabel = new Label();
        private final Label branchLabel = new Label();

        public RepositoryListCell() {
            HBox.setHgrow(this.statusLabel, Priority.ALWAYS);

            this.statusLabel.setMaxWidth(Double.MAX_VALUE);
            this.statusBox.getChildren().addAll(this.statusIconView, this.statusLabel, this.branchLabel, this.branchIconView);

            this.detailsBox.setSpacing(10);

            this.container.setPadding(new Insets(10));
            this.container.getChildren().add(this.statusBox);
            this.container.getChildren().add(this.detailsBox);
        }

        @Override
        protected void updateItem(final RefreshableObjectProperty<RepositoryInfo> item, final boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                this.setGraphic(null);
            } else {
                try {
                    this.setGraphic(this.container);
                    final RepositoryInfo repositoryInfo = item.get();

                    final Repository repository = repositoryInfo.getRepository();

                    this.statusIconView.setImage(new Image("icon:git.svg?size=16&color=999"));
                    this.statusLabel.setText(repositoryInfo.getName());
                    this.branchLabel.setText(repository.getBranch());
                    this.branchLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

                    final Status status = repositoryInfo.getStatus();
                    this.detailsBox.getChildren().clear();

                    final BranchTrackingStatus trackingStatus = repositoryInfo.getBranchTrackingStatus();
                    if (trackingStatus != null) {
                        if (trackingStatus.getAheadCount() == 0 && trackingStatus.getBehindCount() == 0) {
                            this.addLabel(this.detailsBox, "On Track", null, null);
                        } else {
                            if (trackingStatus.getAheadCount() > 0) {
                                this.addLabel(this.detailsBox, "Ahead", String.valueOf(trackingStatus.getAheadCount()), null);
                            }
                            if (trackingStatus.getBehindCount() > 0) {
                                this.addLabel(this.detailsBox, "Behind", String.valueOf(trackingStatus.getBehindCount()), null);
                            }
                        }
                    }

                    final Pane pane = new Pane();
                    HBox.setHgrow(pane, Priority.ALWAYS);
                    this.detailsBox.getChildren().add(pane);
                    if (status.isClean()) {
                        this.addLabel(this.detailsBox, "CLEAN", null, null);
                    } else {
                        final String staged = "#090";
                        final String unstaged = "#F90";
                        final String conflict = "#900";
                        this.addCountLabel(this.detailsBox, status.getAdded().size() + status.getUntracked().size(), "A", staged);
                        this.addCountLabel(this.detailsBox, status.getConflicting().size(), "C", conflict);
                        this.addCountLabel(this.detailsBox, status.getModified().size() + status.getChanged().size(), "M", unstaged);
                        this.addCountLabel(this.detailsBox, status.getMissing().size() + status.getRemoved().size(), "D", unstaged);
                    }

                    this.statusIconView.setImage(new Image("icon:git.svg?size=16&color=0009"));
                } catch (final IOException | NoWorkTreeException exception) {
                    throw new RuntimeException(exception);
                }
            }
        }

        private void addCountLabel(final Pane pane, final int count, final String title, final String color) {
            if (count > 0) {
                this.addLabel(pane, title, String.valueOf(count), null);
            }
        }

        private void addLabel(final Pane pane, final String title, final String content, final String color) {
            final HBox box = new HBox(4);
            box.getStyleClass().add("tag");

            if (color != null) {
                box.setStyle(String.format("-fx-background-color: %1$s;", color));
            }
            if (title != null) {
                final Label titleLabel = new Label(title);
                box.getChildren().add(titleLabel);
            }
            if (content != null) {
                final Label contentLabel = new Label(content);
                box.getChildren().add(contentLabel);
            }
            pane.getChildren().add(box);
        }

    }

}
