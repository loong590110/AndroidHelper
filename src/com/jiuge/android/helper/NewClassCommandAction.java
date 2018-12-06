package com.jiuge.android.helper;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import org.fest.util.Preconditions;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Created by Developer Zailong Shi on 2018/12/6.
 */
public class NewClassCommandAction extends WriteCommandAction<PsiFile> {

    private final PsiFileFactory fileFactory;
    private final JavaDirectoryService directoryService;
    private final String name;
    private final String content;
    private final PsiDirectory directory;

    public NewClassCommandAction(@Nonnull Project project,
                                 @Nonnull String name,
                                 @Nonnull String content,
                                 @Nonnull PsiDirectory directory) {
        super(project);
        this.fileFactory = PsiFileFactory.getInstance(project);
        this.directoryService = JavaDirectoryService.getInstance();
        this.name = Preconditions.checkNotNull(name);
        this.content = Preconditions.checkNotNull(content);
        this.directory = Preconditions.checkNotNull(directory);
    }

    @Override
    protected void run(@NotNull Result<PsiFile> result) throws Throwable {
        final PsiPackage packageElement = directoryService.getPackage(directory);
        if (packageElement == null) {
            throw new RuntimeException("Target directory does not provide a package");
        }

        final String fileName = Extensions.append(name, StdFileTypes.JAVA);
        final PsiFile found = directory.findFile(fileName);
        if (found != null) {
            throw new RuntimeException("Class '" + name + "'already exists in " + packageElement.getName());
        }

//        final String packageName = packageElement.getQualifiedName();
//        final String className = Extensions.remove(this.name, StdFileTypes.JAVA);
//        final String java = String.format("%s,%s,%s", packageName, className, content);
        final String java = content;
        final PsiFile classFile = fileFactory.createFileFromText(fileName, JavaFileType.INSTANCE, java);
        CodeStyleManager.getInstance(classFile.getProject()).reformat(classFile);
        JavaCodeStyleManager.getInstance(classFile.getProject()).optimizeImports(classFile);
        final PsiFile created = (PsiFile) directory.add(classFile);
        result.setResult(created);
    }
}
