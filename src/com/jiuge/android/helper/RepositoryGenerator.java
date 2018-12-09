package com.jiuge.android.helper;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilBase;
import org.apache.http.util.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Zailong Shi on 2018/12/6.
 */
public class RepositoryGenerator extends AnAction {

    private String packageName;
    private String projectName;

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiFile currentEditorFile = PsiUtilBase.getPsiFileInEditor(editor, project);

        projectName = project.getName();
        packageName = ((PsiJavaFile) currentEditorFile).getPackageName();
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
                generateRepositoryCode(project, psiFile, fileName),
                psiFile.getContainingDirectory())
                .execute()
                .hasException();
    }

    private String generateRepositoryCode(Project project, PsiFile psiFile, String className) {
        PsiJavaFile javaFile = (PsiJavaFile) psiFile.getOriginalElement();
        PsiMethod[] methods = javaFile.getClasses()[0].getAllMethods();
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        PsiClass javaClass = elementFactory.createInterface(className);
        for (PsiMethod method : methods) {
            String newMethod = modifyMethodReturnType(method);
            if (newMethod == null)
                continue;
            javaClass.add(elementFactory.createMethodFromText(newMethod, javaClass));
        }
        return getImportListText(javaFile)
                + "\n" + getDocComment()
                + "\n" + javaClass.getText();

    }

    private String modifyMethodReturnType(PsiMethod method) {
        if (method.getReturnTypeElement() == null) {
            return null;
        }
        String text = method.getReturnTypeElement().getText();
        if (!text.contains("Observable<")) {
            return null;
        }
        String returnType = text.substring(text.lastIndexOf('<') + 1, text.indexOf('>'));
        String paramsText = removeAnnotations(method.getParameterList().getText());
        String methodName = method.getName();
        return String.format("RepositoryAccessWrapper<%s> %s(%s);", returnType, methodName, paramsText);
    }

    private CharSequence getImportListText(PsiJavaFile javaFile) {
        StringBuilder importsText = new StringBuilder("import com.lang.mobile.arch.RepositoryAccessWrapper;");
        if (javaFile.getImportList() != null) {
            importsText.append(javaFile.getImportList().getText());
        }
        PsiReferenceList referenceList = javaFile.getClasses()[0].getExtendsList();
        if (referenceList != null) {
            PsiJavaCodeReferenceElement[] elements = referenceList.getReferenceElements();
            for (PsiJavaCodeReferenceElement element : elements) {
                String name = element.getQualifiedName().replace(".", "/");
                String path = getJavaSrcDir(javaFile) + name + ".java";
                VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
                if (virtualFile != null) {
                    PsiJavaFile psiJavaFile = (PsiJavaFile) PsiUtilBase.getPsiFile(element.getProject(),
                            virtualFile);
                    importsText.append(getImportListText(psiJavaFile));
                }
            }
        }
        return importsText;
    }

    private String getJavaSrcDir(PsiJavaFile javaFile) {
        String packageName = javaFile.getPackageName();
        String path = javaFile.getContainingDirectory().getVirtualFile().getPath();
        return path.substring(0, path.length() - packageName.length());
    }

    private String removeAnnotations(String text) {
        String[] fragments = text.trim().substring(1, text.length() - 1).split(" ");
        StringBuilder textBuilder = new StringBuilder();
        for (String fragment : fragments) {
            if (!fragment.startsWith("@")) {
                textBuilder.append(fragment).append(" ");
            }
        }
        return textBuilder.toString();
    }

    private String getDocComment() {
//        String templatePathHolder = "/Users/{user}/Library/Preferences/" +
//                "{product}{version}/fileTemplates/includes/File Header.java";
//        String templatePath = templatePathHolder.replace("{user}", "")
//                .replace("{product}", "")
//                .replace("{version}", "");
        FileTemplate[] template = FileTemplateManager.getDefaultInstance()
                .getTemplates(FileTemplateManager.INCLUDES_TEMPLATES_CATEGORY);
        String comment = "";
        if (template.length > 0) {
            comment = template[0].getText();
        }
        return parseComment(comment);
    }

    private String parseComment(String comment) {
        if (comment != null) {
            String user = System.getenv("USERNAME");
            SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String datetime = datetimeFormat.format(new Date());
            String date = datetime.substring(0, "yyyy/MM/dd".length());
            String time = datetime.substring("yyyy/MM/dd".length() + 1);
            String year = datetime.substring(0, "yyyy".length());
            String month = datetime.substring("yyyy/".length(), "yyyy/".length() + 2);
            String day = datetime.substring("yyyy/MM/".length(), "yyyy/MM/".length() + 2);
            String hour = datetime.substring("yyyy/MM/dd ".length(), "yyyy/MM/dd ".length() + 2);
            String minute = datetime.substring("yyyy/MM/dd HH:".length(), "yyyy/MM/dd HH:".length() + 2);
            comment = comment.replace("${PACKAGE_NAME}", packageName == null ? "" : packageName)
                    .replace("${USER}", user == null ? "" : user)
                    .replace("${DATE}", date)
                    .replace("${TIME}", time)
                    .replace("${YEAR}", year)
                    .replace("${MONTH}", month)
                    .replace("${MONTH_NAME_SHORT}", month)
                    .replace("${MONTH_NAME_FULL}", month)
                    .replace("${DAY}", day)
                    .replace("${DAY_NAME_SHORT}", day)
                    .replace("${DAY_NAME_FULL}", day)
                    .replace("${HOUR}", hour)
                    .replace("${MINUTE}", minute)
                    .replace("${PROJECT_NAME}", projectName == null ? "" : projectName);
        }
        return comment;
    }

}
