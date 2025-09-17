package io.github.jeddict.ai.agent.tools.code;

import dev.langchain4j.agent.tool.Tool;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.openide.util.lookup.Lookups;

public class MoveClassTool extends BaseCodeTool {

    public MoveClassTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "MoveClassTool_moveClass",
        value = "Move a class to another package"
    )
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
