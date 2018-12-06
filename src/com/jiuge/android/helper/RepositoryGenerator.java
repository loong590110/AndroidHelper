package com.jiuge.android.helper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.util.PsiUtilBase;
import org.apache.http.util.TextUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
            if (generateRepositoryFile(project, currentEditorFile, finalFileName)) {
                e.getPresentation().setEnabledAndVisible(true);
            }
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

    private boolean generateRepositoryFile(Project project, PsiFile psiFile, String fileName) {
        return !new NewClassCommandAction(project, fileName,
                generateRepositoryCode(psiFile, fileName),
                psiFile.getContainingDirectory())
                .execute()
                .hasException();
    }

    private String generateRepositoryCode(PsiFile psiFile, String className) {
        PsiJavaFile javaFile = (PsiJavaFile) psiFile.getOriginalElement();
        PsiMethod[] methods = javaFile.getClasses()[0].getMethods();
        PsiJavaFile psiClass = new PsiJavaFileImpl(psiFile.getViewProvider());
        psiClass.setName(className);
        psiClass.setPackageName(javaFile.getPackageName());
        for (PsiMethod method : methods) {
            PsiType returnType = method.getReturnType();
            if (returnType instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) returnType).getActualTypeArguments();
                if (types[0] instanceof ParameterizedType) {
                    types = ((ParameterizedType) returnType).getActualTypeArguments();

                }
            }
            psiClass.add(method);
        }
        return psiClass.getText();
    }
}
