package io.github.jeddict.ai.agent.tools.code;

import dev.langchain4j.agent.tool.Tool;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.util.lookup.Lookups;

public class RenameMethodTool extends BaseCodeTool {

    public RenameMethodTool(final String basedir) {
        super(basedir);
    }

    @Tool(
        name = "RenameMethodTool_renameMethod",
        value = "Rename a method in a Java file"
    )
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
}
