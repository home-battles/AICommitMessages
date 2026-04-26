package com.homebattles.aicommitmessages.providers;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Utility class for getting git diffs from selected files in the IDE.
 */
public class GitDiffProvider {
    private static final Logger LOG = Logger.getInstance(GitDiffProvider.class);
    private final Project project;

    public GitDiffProvider(@NotNull Project project) {
        this.project = project;
    }

    /**
     * Gets the diff for all changed files in the current changelist.
     *
     * @param selectedFiles Optional set of specific files to get diff for
     * @return The combined diff as a string
     */
    public String getDiff(@Nullable Set<VirtualFile> selectedFiles) {
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        LocalChangeList changeList = changeListManager.getDefaultChangeList();
        
        Collection<Change> changedFiles = changeList.getChanges();
        StringBuilder diff = new StringBuilder();

        for (Change change : changedFiles) {
            VirtualFile file = change.getVirtualFile();

            // If specific files are selected, only include those
            if (selectedFiles != null && !selectedFiles.contains(file)) {
                continue;
            }

            String fileDiff = getFileDiff(change);
            if (fileDiff != null && !fileDiff.isEmpty()) {
                diff.append(fileDiff).append("\n");
            }
        }

        var unversionedFiles = changeListManager.getUnversionedFilesPaths();

        for (var unversionedFile : unversionedFiles) {
            VirtualFile file = unversionedFile.getVirtualFile();
            if (file == null) {
                continue;
            }

            if (selectedFiles != null && !selectedFiles.contains(file)) {
                continue;
            }

            diff.append("Index: ").append(file.getPath()).append("\n");
            diff.append("+++ a/").append(file.getName()).append("\n");
            diff.append("Status: Added\n");
            diff.append("+++ Added\n");
            try {
                diff.append(VfsUtilCore.loadText(file)).append("\n");
            } catch (IOException ex) {
                LOG.warn("Error reading unversioned file content: " + file.getPath(), ex);
            }
        }

        return diff.toString();
    }

    /**
     * Gets the diff for a specific file change.
     *
     * @param change The change object containing file diff information
     * @return The diff as a string, or null if unable to retrieve
     */
    @Nullable
    private String getFileDiff(@NotNull Change change) {
        try {
            VirtualFile file = change.getVirtualFile();
            if (file == null) {
                return null;
            }

            // Get the content from the change
            String beforeContent = "";
            String afterContent = "";

            if (change.getBeforeRevision() != null) {
                beforeContent = change.getBeforeRevision().getContent();
            }

            if (change.getAfterRevision() != null) {
                afterContent = change.getAfterRevision().getContent();
            }

            if (beforeContent == null) beforeContent = "";
            if (afterContent == null) afterContent = "";

            // Create a simple diff format
            StringBuilder fileDiff = new StringBuilder();
            fileDiff.append("Index: ").append(file.getPath()).append("\n");
            fileDiff.append("--- a/").append(file.getName()).append("\n");
            fileDiff.append("+++ b/").append(file.getName()).append("\n");

            // For a more complete diff, we could use a diff algorithm here
            // For now, we'll show added/modified/deleted status
            if (beforeContent.isEmpty() && !afterContent.isEmpty()) {
                fileDiff.append("Status: Added\n");
                fileDiff.append(afterContent);
            } else if (!beforeContent.isEmpty() && afterContent.isEmpty()) {
                fileDiff.append("Status: Deleted\n");
                fileDiff.append(beforeContent);
            } else if (!beforeContent.equals(afterContent)) {
                fileDiff.append("Status: Modified\n");
                fileDiff.append("--- Before:\n");
                fileDiff.append(beforeContent).append("\n");
                fileDiff.append("+++ After:\n");
                fileDiff.append(afterContent);
            } else {
                return null; // No actual changes
            }

            return fileDiff.toString();
        } catch (Exception e) {
            LOG.warn("Error getting diff for file", e);
            return null;
        }
    }
}
