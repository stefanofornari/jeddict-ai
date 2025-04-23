/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.github.jeddict.ai.components;

import com.sun.source.tree.*;
import org.netbeans.api.java.source.*;
import org.openide.filesystems.FileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public class MethodUpdater {

    public static void updateMethod(FileObject javaFile, String methodSignature, String newMethodText) {
        try {
            JavaSource javaSource = JavaSource.forFileObject(javaFile);
            javaSource.runModificationTask(copy -> {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                
                CompilationUnitTree cut = copy.getCompilationUnit();
                TreeMaker maker = copy.getTreeMaker();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0); // First class
                
                MethodTree newMethodTree = parseMethodTree(copy, methodSignature, newMethodText);
                if (newMethodTree == null) {
                    return;
                }
                
                for (Tree member : classTree.getMembers()) {
                    if (member instanceof MethodTree method) {
                        String sig = method.getName() + method.getParameters().stream()
                                .map(p -> p.getType().toString())
                                .collect(java.util.stream.Collectors.joining(",", "(", ")"));
                        if (sig.equals(methodSignature)) {
                            copy.rewrite(method, newMethodTree);
                            return;
                        }
                    }
                }
            }).commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static MethodTree parseMethodTree(WorkingCopy copy, String expectedSignature, String methodText) throws IOException {
        String dummyClass = "public class Dummy { " + methodText + " }";
        FileObject tempFile = FileUtil.createMemoryFileSystem().getRoot().createData("Dummy.java");
        try (OutputStream os = tempFile.getOutputStream(); Writer writer = new OutputStreamWriter(os)) {
            writer.write(dummyClass);
        }

        JavaSource source = JavaSource.forFileObject(tempFile);
        final MethodTree[] result = new MethodTree[1];

        source.runUserActionTask(controller -> {
            controller.toPhase(JavaSource.Phase.RESOLVED);
            CompilationUnitTree cut = controller.getCompilationUnit();
            for (Tree typeDecl : cut.getTypeDecls()) {
                if (typeDecl instanceof ClassTree classTree) {
                    for (Tree member : classTree.getMembers()) {
                        if (member instanceof MethodTree method) {
                            String sig = method.getName() + method.getParameters().stream()
                                    .map(p -> p.getType().toString())
                                    .collect(java.util.stream.Collectors.joining(",", "(", ")"));
                            
                            if (sig.equals(expectedSignature)) {
                                result[0] = method;
                                return;
                            }
                        }
                    }
                }
            }
        }, true);
        tempFile.delete();

        if (result[0] == null) {
            return null;
        }

        TreeMaker maker = copy.getTreeMaker();
        MethodTree mt = result[0];

        // Correcting annotations
        List<? extends AnnotationTree> correctedAnnotations = mt.getModifiers().getAnnotations().stream()
                .map(annotation -> {
                    if (annotation.getArguments() != null && !annotation.getArguments().isEmpty()) {
                        List<ExpressionTree> args = new ArrayList<>();
                        for (ExpressionTree argument : annotation.getArguments()) {
                            if (argument instanceof AssignmentTree assignment) {
                                if ("value".equals(assignment.getVariable().toString())) {
                                    ExpressionTree valueExpr = assignment.getExpression();
                                    ExpressionTree safeExpr = valueExpr;

                                    if (valueExpr instanceof LiteralTree literal) {
                                        Object val = literal.getValue();
                                        // Unquote if needed, though usually it's already clean
                                        if (val instanceof String strVal) {
                                            safeExpr = maker.Literal(strVal);
                                        }
                                    }

                                    args.add(safeExpr);
                                } else {
                                    args.add(argument);
                                }

                            } else {
                                args.add(argument);
                            }
                        }
                        return maker.Annotation(annotation.getAnnotationType(), args);
                    }
                    return annotation;
                }).collect(Collectors.toList());

        // Update the method's modifiers with the corrected annotations
        ModifiersTree updatedModifiers = maker.Modifiers(
                mt.getModifiers().getFlags(), correctedAnnotations);

        return maker.Method(
                updatedModifiers,
                mt.getName(),
                mt.getReturnType(),
                mt.getTypeParameters(),
                mt.getParameters(),
                mt.getThrows(),
                mt.getBody(),
                (ExpressionTree) mt.getDefaultValue()
        );
    }

}
