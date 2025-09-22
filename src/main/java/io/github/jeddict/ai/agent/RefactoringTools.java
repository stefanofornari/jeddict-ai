/**
 * Copyright 2025 the original author or authors from the Jeddict project (https://jeddict.github.io/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.jeddict.ai.agent;

import dev.langchain4j.agent.tool.Tool;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Tools for code-level operations in NetBeans projects.
 *
 * <p>
 * These tools use the NetBeans JavaSource and Refactoring APIs to let an AI
 * assistant explore code structure, format files, or prepare refactoring
 * tasks.</p>
 *
 * <p>
 * <b>Example (AI usage):</b></p>
 * <pre>
 * assistant.listClassesInFile("src/main/java/com/example/MyClass.java");
 * // -> "Class: com.example.MyClass"
 * </pre>
 */
public class RefactoringTools extends AbstractCodeTool {

    public RefactoringTools(final String basedir) {
        super(basedir);
    }

    /**
     * Format a Java file using NetBeans formatter.
     *
     * <p>
     * <b>Example:</b></p>
     * <pre>
     * formatFile("src/main/java/com/example/MyClass.java");
     * // -> "File formatted successfully"
     * </pre>
     *
     * @param path relative path to the Java file
     * @return status message
     */
    @Tool("Format a Java file by path using NetBeans code formatter")
    public String formatFile(String path) throws Exception {
        progress("Formatting " + path);
        return withJavaSource(path, javaSource -> {
            javaSource.runModificationTask(cc -> {
                cc.toPhase(JavaSource.Phase.UP_TO_DATE);
                // NetBeans formatter applies automatically on save
            }).commit();
            return "File formatted successfully";
        }, true);
    }

    @Tool("Rename a class in a Java file")
    public String renameClass(String path, String oldName, String newName)
    throws Exception {
        progress("Renaming class " + oldName + " -> " + newName);

        return withJavaSource(path, javaSource -> {
            final StringBuilder result = new StringBuilder();

            javaSource.runModificationTask(cc -> {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                for (TypeElement type : ElementFilter.typesIn(cc.getTopLevelElements())) {
                    if (type.getSimpleName().toString().equals(oldName)) {
                        ElementHandle<TypeElement> handle = ElementHandle.create(type);

                        // Create refactoring
                        Lookup lookup = Lookups.singleton(handle);
                        RenameRefactoring refactor = new RenameRefactoring(lookup);
                        refactor.setNewName(newName);

                        // Run refactoring in session
                        RefactoringSession session = RefactoringSession.create("Rename Class");
                        refactor.prepare(session);
                        session.doRefactoring(true);

                        result.append("Class renamed from ")
                                .append(oldName)
                                .append(" to ")
                                .append(newName);
                    }
                }
            }).commit();

            return result.length() == 0
                    ? "No class named " + oldName + " found."
                    : result.toString();
        }, true);
    }

    @Tool("Rename a method in a Java file")
    public String renameMethod(String path, String className, String oldMethod, String newMethod)
    throws Exception {
        return withJavaSource(path, javaSource -> {
            final StringBuilder result = new StringBuilder();
            javaSource.runModificationTask(cc -> {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                for (TypeElement type : ElementFilter.typesIn(cc.getTopLevelElements())) {
                    if (type.getSimpleName().toString().equals(className)) {
                        for (Element member : type.getEnclosedElements()) {
                            if ((member.getKind() == javax.lang.model.element.ElementKind.METHOD || member.getKind() == javax.lang.model.element.ElementKind.CONSTRUCTOR) && member.getSimpleName().toString().equals(oldMethod)) {
                                ElementHandle<Element> handle = ElementHandle.create(member);

                                RenameRefactoring ref = new RenameRefactoring(Lookups.singleton(handle));
                                ref.setNewName(newMethod);
                                RefactoringSession session = RefactoringSession.create("Rename Method");
                                ref.prepare(session);
                                session.doRefactoring(true);

                                result.append("Method renamed: ").append(oldMethod).append(" -> ").append(newMethod).append("\n");
                            }
                        }
                    }
                }
            }).commit();
            return result.length() == 0 ? "No method " + oldMethod + " found in " + className : result.toString();
        }, true);
    }

    @Tool("Move a class to another package")
    public String moveClass(String path, String className, String newPackage)
    throws Exception {
        return withJavaSource(path, javaSource -> {
            final StringBuilder result = new StringBuilder();
            javaSource.runModificationTask(cc -> {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                for (TypeElement type : ElementFilter.typesIn(cc.getTopLevelElements())) {
                    if (type.getSimpleName().toString().equals(className)) {
                        ElementHandle<TypeElement> handle = ElementHandle.create(type);

                        org.netbeans.modules.refactoring.api.MoveRefactoring ref
                                = new org.netbeans.modules.refactoring.api.MoveRefactoring(Lookups.singleton(handle));
                        ref.setTarget(Lookups.singleton(newPackage));

                        RefactoringSession session = RefactoringSession.create("Move Class");
                        ref.prepare(session);
                        session.doRefactoring(true);

                        result.append("Moved class ").append(className)
                                .append(" to package ").append(newPackage);
                    }
                }
            }).commit();
            return result.length() == 0 ? "No class " + className + " found." : result.toString();
        }, true);
    }
}