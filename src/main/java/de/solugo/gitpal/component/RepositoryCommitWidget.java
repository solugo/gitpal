package de.solugo.gitpal.component;

import de.solugo.gitpal.util.RefreshableObjectProperty;
import de.solugo.gitpal.util.RepositoryInfo;
import de.solugo.gitpal.util.Util;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.UserConfig;

/**
 *
 * @author Frederic Kneier
 */
public class RepositoryCommitWidget extends Widget {

    @FXML
    private ListView<String> listView;

    @FXML
    private CheckBox listViewCheckBox;

    @FXML
    private TextField commitAuthorName;

    @FXML
    private TextField commitAuthorEmail;

    @FXML
    private TextArea commitMessage;

    @FXML
    private Button commitButton;

    private final RefreshableObjectProperty< RepositoryInfo> repositoryInfoProperty;

    public RepositoryCommitWidget(final RefreshableObjectProperty< RepositoryInfo> repositoryInfoProperty) {
        this.repositoryInfoProperty = repositoryInfoProperty;
    }

    @FXML
    public void initialize() {
        this.listView.setCellFactory((l) -> new StageListCell());

        this.repositoryInfoProperty.addListener((oberserver, old, value) -> {
            this.refresh();
        });

        this.commitButton.setOnAction((e) -> this.commit());
        this.refresh();
    }

    private void refresh() {
        final RepositoryInfo value = this.repositoryInfoProperty.get();
        if (value != null) {
            this.listViewCheckBox.setOnAction((e) -> {
                final RepositoryInfo repositoryInfo = this.repositoryInfoProperty.get();
                if (!repositoryInfo.getUnstaged().isEmpty()) {
                    this.stage(repositoryInfo.getUnstaged());
                } else {
                    this.unstage(repositoryInfo.getStaged());
                }
            });
            
            this.commitButton.setDisable(value.getStaged().isEmpty());

            final UserConfig userConfig = value.getRepository().getConfig().get(UserConfig.KEY);
            this.commitAuthorName.setText(userConfig.getAuthorName());
            this.commitAuthorEmail.setText(userConfig.getAuthorEmail());
            this.commitMessage.setText("");
            this.listView.getItems().clear();

            this.listView.getItems().addAll(value.getStaged());
            this.listView.getItems().addAll(value.getUnstaged());
            Collections.sort(this.listView.getItems());
            this.listViewCheckBox.setSelected(value.getUnstaged().isEmpty());
        }
    }

    private void commit() {
        try {
            final Git git = new Git(this.repositoryInfoProperty.get().getRepository());
            final CommitCommand commitCommand = git.commit();
            commitCommand.setAuthor(commitAuthorName.getText(), commitAuthorEmail.getText());
            commitCommand.setMessage(this.commitMessage.getText());
            commitCommand.call();
            this.repositoryInfoProperty.refresh();
        } catch (final GitAPIException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void unstage(final Collection<String> items) {
        Util.runUi(() -> {
            final Git git = new Git(this.repositoryInfoProperty.get().getRepository());
            final ResetCommand resetCommand = git.reset();
            for (final String item : items) {
                resetCommand.addPath(item);
            }
            resetCommand.call();
            this.repositoryInfoProperty.refresh();
        });
    }

    private void stage(final Collection<String> items) {
        Util.runUi(() -> {
            final Git git = new Git(this.repositoryInfoProperty.get().getRepository());
            final AddCommand addCommand = git.add();
            for (final String item : items) {
                addCommand.addFilepattern(item);
            }
            addCommand.call();
            this.repositoryInfoProperty.refresh();
        });
    }

    private class StageListCell extends ListCell<String> {

        @Override
        protected void updateItem(final String item, final boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                this.setText(null);
                this.setGraphic(null);
            } else {
                final RepositoryInfo repositoryInfo = repositoryInfoProperty.get();
                this.setText(item);
                final CheckBox checkBox = new CheckBox();
                checkBox.setSelected(repositoryInfo.getStaged().contains(item));
                checkBox.setOnAction((e) -> {
                    if (checkBox.isSelected()) {
                        RepositoryCommitWidget.this.stage(Arrays.asList(item));
                    } else {
                        RepositoryCommitWidget.this.unstage(Arrays.asList(item));
                    }
                });
                this.setGraphic(checkBox);
            }
        }

    }

}
