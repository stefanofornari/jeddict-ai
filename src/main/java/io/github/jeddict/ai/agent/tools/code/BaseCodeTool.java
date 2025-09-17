package io.github.jeddict.ai.agent.tools.code;

import io.github.jeddict.ai.agent.tools.AbstractTool;
import io.github.jeddict.ai.util.DocAction;
import io.github.jeddict.ai.util.ThrowingFunction;
import java.nio.file.Path;
import javax.swing.text.Document;
import org.netbeans.api.java.source.JavaSource;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

public class BaseCodeTool extends AbstractTool {

    public BaseCodeTool(final String basedir) {
        super(basedir);
    }

    /**
     * Executes an action on a JavaSource obtained from a file in the project.
     *
     * @param project  the NetBeans project
     * @param fullpath     relative path of the Java file
     * @param action   function to run with JavaSource, returns a result
     * @param modify   if true, will open the document in modification mode
     * @param <T>      result type
     * @return the result of the action, or error message if failed
     */
    protected <T> T withJavaSource(
        String path,
        ThrowingFunction<JavaSource, T> action,
        boolean modify
    ) throws Exception {
        Path fullPath = fullPath(path);
        FileObject fo = org.openide.filesystems.FileUtil.toFileObject(fullPath);
        if (fo == null) {
            return (T) ("File not found: " + path);
        }

        JavaSource javaSource = JavaSource.forFileObject(fo);
        if (javaSource == null) {
            return (T) ("Not a Java source file: " + path);
        }

        return action.apply(javaSource);
    }

    /**
     * Utility method that opens a NetBeans document for a given path and
     * applies the specified action.
     * <p>
     * This centralizes error handling and ensures that the document is saved
     * after the action is applied.
     *
     * @param path the relative or absolute path to the file
     * @param action the action to perform on the opened {@link Document}
     * @param save save the doc after post action
     * @return the result of the action, or an error message
     */
    public String withDocument(String path, DocAction action, boolean save)
    throws Exception {
        FileObject fo = org.openide.filesystems.FileUtil.toFileObject(fullPath(path));
        if (fo == null) {
            return "File not found: " + path;
        }

        DataObject dobj = DataObject.find(fo);
        EditorCookie cookie = dobj.getLookup().lookup(EditorCookie.class);
        if (cookie == null) {
            return "No editor available for: " + path;
        }

        Document doc = cookie.openDocument();
        String result = action.apply(doc);
        if (save) {
            cookie.saveDocument();
        }
        return result;
    }
}
