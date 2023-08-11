package com.github.leondevlifelog.gitea.tasks;

import com.github.leondevlifelog.gitea.GiteaBundle;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.tasks.TaskBundle;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.config.BaseRepositoryEditor;
import com.intellij.tasks.config.TaskRepositoriesConfigurable;
import com.intellij.tasks.impl.RequestFailedException;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.GridBag;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

class GiteaRepositoryEditor extends BaseRepositoryEditor<GiteaRepository> {
    private final Project project;
    private MyTextField myRepoAuthor;
    private MyTextField myRepoName;
    private MyTextField myToken;
    private JBCheckBox myShowNotAssignedIssues;
    private JButton myTokenButton;
    private JBLabel myHostLabel;
    private JBLabel myRepositoryLabel;
    private JBLabel myTokenLabel;
    static final String TASKS_NOTIFICATION_GROUP = "Task Group";

    GiteaRepositoryEditor(final Project project, final GiteaRepository repository, Consumer<? super GiteaRepository> changeListener) {
        super(project, repository, changeListener);
        this.project = project;
        myUrlLabel.setVisible(false);
        myUsernameLabel.setVisible(false);
        myUserNameText.setVisible(false);
        myPasswordLabel.setVisible(false);
        myPasswordText.setVisible(false);
        myUseHttpAuthenticationCheckBox.setVisible(false);

        myRepoAuthor.setText(repository.getRepoAuthor());
        myRepoName.setText(repository.getRepoName());
        myToken.setText(repository.getPassword());
        myShowNotAssignedIssues.setSelected(!repository.getAssigned());

        DocumentListener buttonUpdater = new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                updateTokenButton();
            }
        };

        myURLText.getDocument().addDocumentListener(buttonUpdater);
        myRepoAuthor.getDocument().addDocumentListener(buttonUpdater);
        myRepoName.getDocument().addDocumentListener(buttonUpdater);
    }

    @Nullable
    @Override
    protected JComponent createCustomPanel() {
        myHostLabel = new JBLabel(GiteaBundle.message("task.repo.host.field"), SwingConstants.RIGHT);

        JPanel myHostPanel = new JPanel(new BorderLayout(5, 0));
        myHostPanel.add(myURLText, BorderLayout.CENTER);
        myShareUrlCheckBox.setText(GiteaBundle.message("skip.ssl.verify"));
        myShareUrlCheckBox.setVisible(false);
        myHostPanel.add(myShareUrlCheckBox, BorderLayout.EAST);

        myRepositoryLabel = new JBLabel(GiteaBundle.message("task.repo.repository.field"), SwingConstants.RIGHT);
        myRepoAuthor = new MyTextField(GiteaBundle.message("task.repo.owner.field.empty.hint"));
        myRepoName = new MyTextField(GiteaBundle.message("task.repo.name.field.empty.hint"));
        myRepoAuthor.setPreferredSize("SomelongNickname");
        myRepoName.setPreferredSize("SomelongReponame-with-suffixes");

        JPanel myRepoPanel = new JPanel(new GridBagLayout());
        GridBag bag = new GridBag().setDefaultWeightX(1).setDefaultFill(GridBagConstraints.HORIZONTAL);
        myRepoPanel.add(myRepoAuthor, bag.nextLine().next());
        myRepoPanel.add(new JLabel("/"), bag.next().fillCellNone().insets(0, 5, 0, 5).weightx(0));
        myRepoPanel.add(myRepoName, bag.next());

        myTokenLabel = new JBLabel(GiteaBundle.message("task.repo.token.field"), SwingConstants.RIGHT);
        myToken = new MyTextField(GiteaBundle.message("task.repo.token.field.empty.hint"));
        myTokenButton = new JButton(GiteaBundle.message("task.repo.token.create.button"));
        myTokenButton.addActionListener(e -> {
            String urlText = myURLText.getText();
            try {
                URL url = new URL(urlText);
                URI uri = url.toURI();
                uri = uri.resolve("/user/settings/applications");
                BrowserUtil.browse(uri);
            } catch (MalformedURLException | URISyntaxException ex) {
                String content = "<p>" + GiteaBundle.message("notification.title.invalid.url") + "</p>" + urlText;
                new Notification(TASKS_NOTIFICATION_GROUP, GiteaBundle.message("notification.title.invalid.url"), content, NotificationType.WARNING).notify(project);
            }
        });

        JPanel myTokenPanel = new JPanel();
        myTokenPanel.setLayout(new BorderLayout(5, 5));
        myTokenPanel.add(myToken, BorderLayout.CENTER);
        myTokenPanel.add(myTokenButton, BorderLayout.EAST);

        myShowNotAssignedIssues = new JBCheckBox(VcsBundle.message("checkbox.include.issues.not.assigned.to.me"));

        installListener(myRepoAuthor);
        installListener(myRepoName);
        installListener(myToken);
        installListener(myShowNotAssignedIssues);

        return FormBuilder.createFormBuilder().setAlignLabelOnRight(true).addLabeledComponent(myHostLabel, myHostPanel).addLabeledComponent(myRepositoryLabel, myRepoPanel).addLabeledComponent(myTokenLabel, myTokenPanel).addComponentToRightColumn(myShowNotAssignedIssues).getPanel();
    }

    @Override
    public void apply() {
        super.apply();
        myRepository.setRepoName(getRepoName());
        myRepository.setRepoAuthor(getRepoAuthor());
        myRepository.setPassword(getToken());
        myRepository.storeCredentials();
        myRepository.setAssigned(isAssignedIssuesOnly());
    }

    @Override
    public void setAnchor(@Nullable final JComponent anchor) {
        super.setAnchor(anchor);
        myHostLabel.setAnchor(anchor);
        myRepositoryLabel.setAnchor(anchor);
        myTokenLabel.setAnchor(anchor);
    }

    private void updateTokenButton() {
        if (StringUtil.isEmptyOrSpaces(getHost()) || StringUtil.isEmptyOrSpaces(getRepoAuthor()) || StringUtil.isEmptyOrSpaces(getRepoName())) {
            myTokenButton.setEnabled(false);
        } else {
            myTokenButton.setEnabled(true);
        }
    }

    @NotNull
    private String getHost() {
        return myURLText.getText().trim();
    }

    @NotNull
    private String getRepoAuthor() {
        return myRepoAuthor.getText().trim();
    }

    @NotNull
    private String getRepoName() {
        return myRepoName.getText().trim();
    }

    @NotNull
    private String getToken() {
        return myToken.getText().trim();
    }

    private boolean isAssignedIssuesOnly() {
        return !myShowNotAssignedIssues.isSelected();
    }

    public static class MyTextField extends JBTextField {
        private int myWidth = -1;

        public MyTextField(@Nls @NotNull String hintCaption) {
            getEmptyText().setText(hintCaption);
        }

        public void setPreferredSize(@NotNull String sampleSizeString) {
            myWidth = getFontMetrics(getFont()).stringWidth(sampleSizeString);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            if (myWidth != -1) {
                size.width = myWidth;
            }
            return size;
        }
    }
}