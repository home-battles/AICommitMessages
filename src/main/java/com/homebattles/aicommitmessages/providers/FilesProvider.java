package com.homebattles.aicommitmessages.providers;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A utility class that provides methods for retrieving files selected in a commit workflow UI.
 */
public class FilesProvider {

    /**
     * Gets the selected files from the commit workflow UI.
     * Includes both versioned changes and unversioned files.
     *
     * @param e The action event containing commit workflow data
     * @return A set of selected virtual files
     */
    public static @NotNull Set<VirtualFile> getSelectedFiles(@NotNull AnActionEvent e) {
        var selectedChanges = Objects.requireNonNull(e.getData(VcsDataKeys.COMMIT_WORKFLOW_UI)).getIncludedChanges();

        var selectedUnversionedChanges = Objects.requireNonNull(e.getData(VcsDataKeys.COMMIT_WORKFLOW_UI)).getIncludedUnversionedFiles();


        Set<VirtualFile> selectedFiles = new HashSet<>();

        for (Change change : selectedChanges) {
            VirtualFile file = change.getVirtualFile();
            if (file != null) {
                selectedFiles.add(file);
            }
        }

        for (FilePath unversionedFile : selectedUnversionedChanges) {
            VirtualFile file = unversionedFile.getVirtualFile();

            if (file != null) {
                selectedFiles.add(file);
            }
        }

        return selectedFiles;
    }
}

