package de.solugo.gitpal.component;

import de.solugo.gitpal.context.TabContext;
import de.solugo.gitpal.util.RefreshableObjectProperty;
import de.solugo.gitpal.util.RepositoryInfo;
import de.solugo.gitpal.util.Util;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

/**
 *
 * @author Frederic Kneier
 */
public class RepositoryDetailsWidget extends Widget {

    private final TabContext tabContext;
    private final RefreshableObjectProperty< RepositoryInfo> repositoryInfoProperty;
    private final TreeItem<Object> rootItem = new TreeItem<>();
    private final TreeItem<Object> filesStatusItem = new TreeItem<>("Status");
    private final TreeItem<Object> branchsItem = new TreeItem<>(new LocalBranchList("Branches"));
    private final TreeItem<Object> tagsItem = new TreeItem<>(new TagList("Tags"));
    private final TreeItem<Object> remotesItem = new TreeItem<>(new RemoteList("Remotes"));

    @FXML
    private TreeView<Object> treeView;

    @FXML
    private Pane contentPane;

    @FXML
    private Button refreshButton;

    @FXML
    private Button pullButton;

    @FXML
    private Button pushButton;

    @FXML
    private Button fetchButton;

    @FXML
    private Button commitButton;

    @FXML
    private Button revertButton;

    @FXML
    private Button stashButton;

    public RepositoryDetailsWidget(final TabContext tabContext, final RefreshableObjectProperty< RepositoryInfo> repositoryInfoProperty) {
        this.tabContext = tabContext;
        this.repositoryInfoProperty = repositoryInfoProperty;
    }

    @FXML
    protected void initialize() {
        this.treeView.setShowRoot(false);
        this.treeView.setRoot(rootItem);
        this.treeView.setOnMouseClicked(this::onItemClicked);
        this.treeView.setCellFactory((t) -> new ExtendedTreeCell());

        this.rootItem.getChildren().add(filesStatusItem);
        this.rootItem.getChildren().add(branchsItem);
        this.rootItem.getChildren().add(remotesItem);
        this.rootItem.getChildren().add(tagsItem);

        this.refreshButton.setOnAction((e) -> {
            this.repositoryInfoProperty.refresh();
        });
        this.refreshButton.setOnAction((e) -> {
            Util.runUi(() -> this.repositoryInfoProperty.get().getGit().stashCreate().call());
            this.repositoryInfoProperty.refresh();
        });
        this.revertButton.setOnAction((e) -> {
            Util.runUi(() -> this.repositoryInfoProperty.get().getGit().revert().call());
            this.repositoryInfoProperty.refresh();
        });

        this.repositoryInfoProperty.addListener((observable, old, value) -> {
            this.refresh();
        });
        this.refresh();
    }

    private void refresh() {
        final RepositoryInfo value = this.repositoryInfoProperty.get();
        if (value != null) {

            this.filesStatusItem.getChildren().clear();
            final TreeItem<Object> headItem = new TreeItem<>(new Work("HEAD"));
            headItem.setGraphic(new ImageView("icon:work.svg?size=16&color=0009"));
            filesStatusItem.getChildren().add(headItem);

            this.branchsItem.getChildren().clear();
            for (final Map.Entry<String, Ref> entry : value.getLocalBranches().entrySet()) {
                final TreeItem<Object> branchItem = new TreeItem<>(new LocalBranch(entry.getKey(), entry.getValue()));
                branchItem.setGraphic(new ImageView("icon:branch.svg?size=16&color=0009"));
                this.branchsItem.getChildren().add(branchItem);
            }

            this.tagsItem.getChildren().clear();
            for (final Map.Entry<String, Ref> entry : value.getTags().entrySet()) {
                final TreeItem<Object> tagItem = new TreeItem<>(new Tag(entry.getKey(), entry.getValue()));
                tagItem.setGraphic(new ImageView("icon:tag.svg?size=16&color=0009"));
                this.tagsItem.getChildren().add(tagItem);
            }

            this.remotesItem.getChildren().clear();
            for (final String remote : value.getRepository().getRemoteNames()) {
                final TreeItem<Object> remoteItem = new TreeItem<>(new Remote(remote));
                remoteItem.setGraphic(new ImageView("icon:remote.svg?size=16&color=0009"));
                for (final Map.Entry<String, Ref> entry : value.getRemoteBranches(remote).entrySet()) {
                    final TreeItem<Object> remoteBranchItem = new TreeItem<>(new RemoteBranch(entry.getKey(), entry.getValue()));
                    remoteBranchItem.setGraphic(new ImageView("icon:branch.svg?size=16&color=0009"));
                    remoteItem.getChildren().add(remoteBranchItem);
                }
                this.remotesItem.getChildren().add(remoteItem);
            }

            this.revertButton.setDisable(value.getStatus().isClean());
            this.stashButton.setDisable(value.getStatus().isClean());

            this.filesStatusItem.setExpanded(true);
            this.branchsItem.setExpanded(true);
            this.remotesItem.setExpanded(true);

            if (this.contentPane.getChildren().isEmpty()) {
                this.onItemClicked(headItem);
            }
        }
    }

    private void onItemClicked(final MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            this.onItemClicked(this.treeView.getSelectionModel().getSelectedItem());
        }
    }

    private void onItemClicked(final TreeItem<Object> item) {
        if (item != null && item.getValue() instanceof Work) {
            this.contentPane.getChildren().clear();
            this.contentPane.getChildren().add(new RepositoryCommitWidget(repositoryInfoProperty).getRoot());
        }
    }

    private static class LocalBranchList {

        private final String title;

        public LocalBranchList(final String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }

    }

    private static class Work {

        private final String title;

        public Work(final String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }

    }

    private static class TagList {

        private final String title;

        public TagList(final String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }

    }

    private static class RemoteList {

        private final String title;

        public RemoteList(final String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }

    }

    private static class Tag {

        private final String title;
        private final Ref ref;

        public Tag(final String title, final Ref ref) {
            this.title = title;
            this.ref = ref;
        }

        @Override
        public String toString() {
            return title;
        }

        public Ref getRef() {
            return ref;
        }

    }

    private static class LocalBranch {

        private final String title;
        private final Ref ref;

        public LocalBranch(final String title, final Ref ref) {
            this.title = title;
            this.ref = ref;
        }

        @Override
        public String toString() {
            return title;
        }

        public Ref getRef() {
            return ref;
        }

    }

    private static class Remote {

        private final String title;

        public Remote(final String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }

    }

    private static class RemoteBranch {

        private final String title;
        private final Ref ref;

        public RemoteBranch(final String title, final Ref ref) {
            this.title = title;
            this.ref = ref;
        }

        @Override
        public String toString() {
            return title;
        }

        public Ref getRef() {
            return ref;
        }

    }

    private class ExtendedTreeCell extends TextFieldTreeCell<Object> {

        @Override
        public void updateItem(final Object item, boolean empty) {
            super.updateItem(item, empty);

            if (item instanceof LocalBranchList) {
                final MenuItem deleteItem = new MenuItem("Create");
                deleteItem.setOnAction((e) -> {

                });
                this.setContextMenu(new ContextMenu(deleteItem));
            } else if (item instanceof TagList) {
                final MenuItem deleteItem = new MenuItem("Create");
                deleteItem.setOnAction((e) -> {

                });
                this.setContextMenu(new ContextMenu(deleteItem));
            } else if (item instanceof RemoteList) {
                final MenuItem deleteItem = new MenuItem("Add");
                deleteItem.setOnAction((e) -> {

                });
                this.setContextMenu(new ContextMenu(deleteItem));
            } else if (item instanceof Tag) {
                final MenuItem checkoutItem = new MenuItem("Checkout");
                checkoutItem.setOnAction((e) -> {

                });
                final MenuItem deleteItem = new MenuItem("Delete");
                deleteItem.setOnAction((e) -> {

                });
                this.setContextMenu(new ContextMenu(checkoutItem, new SeparatorMenuItem(), deleteItem));
            } else if (item instanceof LocalBranch) {
                final LocalBranch localBranch = (LocalBranch) item;
                final MenuItem checkoutItem = new MenuItem("Checkout");
                checkoutItem.setOnAction((e) -> {
                    Util.runUi(() -> {
                        repositoryInfoProperty.get().getGit().checkout().setName(localBranch.getRef().getName()).call();
                    });
                });
                final MenuItem deleteItem = new MenuItem("Delete");
                deleteItem.setOnAction((e) -> {

                });
                this.setContextMenu(new ContextMenu(checkoutItem, new SeparatorMenuItem(), deleteItem));
            } else if (item instanceof Remote) {
                final Remote remote = (Remote) item;
                final MenuItem pushItem = new MenuItem("Push");
                pushItem.setOnAction((e) -> {
                    Util.runUi(() -> {
                        repositoryInfoProperty.get().getGit().push().setRemote(remote.toString()).setCredentialsProvider(null).call();
                        repositoryInfoProperty.refresh();
                    });
                });
                final MenuItem pullItem = new MenuItem("Pull");
                pullItem.setOnAction((e) -> {
                    Util.runUi(() -> {
                        repositoryInfoProperty.get().getGit().pull().setRemote(remote.toString()).call();
                        repositoryInfoProperty.refresh();
                    });
                });
                final MenuItem fetchItem = new MenuItem("Fetch");
                fetchItem.setOnAction((e) -> {
                    Util.runUi(() -> {
                        repositoryInfoProperty.get().getGit().fetch().setRemote(remote.toString()).call();
                        repositoryInfoProperty.refresh();
                    });
                });
                final MenuItem deleteItem = new MenuItem("Delete");
                deleteItem.setOnAction((e) -> {

                });
                this.setContextMenu(new ContextMenu(pushItem, pullItem, fetchItem, new SeparatorMenuItem(), deleteItem));
            } else if (item instanceof RemoteBranch) {
                final RemoteBranch remoteBranch = (RemoteBranch) item;
                final MenuItem checkoutItem = new MenuItem("Checkout");
                checkoutItem.setOnAction((e) -> {
                    try {
                        final Git git = new Git(RepositoryDetailsWidget.this.repositoryInfoProperty.get().getRepository());
                        git.checkout().setName(remoteBranch.getRef().getName()).call();
                    } catch (final GitAPIException exception) {
                        throw new RuntimeException(exception);
                    }
                });
                this.setContextMenu(new ContextMenu(checkoutItem));
            } else {
                this.setContextMenu(null);
            }

        }

    }
}
