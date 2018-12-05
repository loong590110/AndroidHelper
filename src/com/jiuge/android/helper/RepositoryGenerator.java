package com.jiuge.android.helper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilBase;
import org.apache.http.util.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Zailong Shi on 2018/12/6.
 */
public class RepositoryGenerator extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile currentEditorFile = PsiUtilBase.getPsiFileInEditor(editor, project);

        String currentEditorFileName = currentEditorFile.getName();
        String generateFileName = generateFileName(currentEditorFileName);
        String finalFileName = showInputDialog(generateFileName);
        if (!TextUtils.isEmpty(finalFileName)) {
            generateRepositoryFile(project, currentEditorFile, finalFileName);
        }
    }

    private String generateFileName(String fileName) {
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.indexOf('.'));
        }
        String postfixFileName = "Repository";
        int end = fileName.indexOf("Service");
        if (end == 0) {
            return postfixFileName;
        } else if (end > 0) {
            return fileName.substring(0, end) + postfixFileName;
        }
        return fileName + postfixFileName;
    }

    private String showInputDialog(String generateFileName) {
        return Messages.showInputDialog("编辑你要生成Repository类的名称：",
                "Generate Repository",
                null,
                generateFileName,
                null);
    }

    private void generateRepositoryFile(Project project, PsiFile psiFile, String fileName) {
        new WriteCommandAction.Simple(project, psiFile) {
            @Override
            protected void run() {
                PsiDirectory psiDirectory = psiFile.getContainingDirectory();
                String fullFileName = psiDirectory.getVirtualFile().getPath()
                        + File.separator + fileName + ".java";
                try (FileOutputStream os = new FileOutputStream(fullFileName)) {
                    String code = generateRepositoryCode(project, psiFile);
                    byte[] buffer = code.getBytes();
                    os.write(buffer);
                } catch (NullPointerException | IOException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private String generateRepositoryCode(Project project, PsiFile psiFile) {
        StringBuilder stringBuilder = new StringBuilder(project.getBaseDir().getPath());
        return stringBuilder.toString();
    }
}
