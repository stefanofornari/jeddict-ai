package io.github.jeddict.ai.agent.tools.project;

import io.github.jeddict.ai.agent.tools.AbstractTool;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public abstract class BaseBuildTool extends AbstractTool {

    protected final static Logger logger = Logger.getLogger(BaseBuildTool.class.getName());

    public BaseBuildTool(final String basedir) {
        super(basedir);
    }

    public FileObject projectFileObject() {
        return FileUtil.toFileObject(basepath);
    }
}
