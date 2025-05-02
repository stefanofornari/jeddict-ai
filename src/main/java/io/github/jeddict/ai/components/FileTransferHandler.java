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
package io.github.jeddict.ai.components;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author Gaurav Gupta
 */
public class FileTransferHandler {
    
    public static void register(JComponent comp, Consumer<FileObject> callback){
        TransferHandler defaultHandler = comp.getTransferHandler();
        comp.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                try {
                    Transferable t = support.getTransferable();
                    for (DataFlavor flavor : t.getTransferDataFlavors()) {
                        if (flavor.isFlavorJavaFileListType()
                                || DataObject[].class.equals(flavor.getRepresentationClass())
                                || Node.class.isAssignableFrom(flavor.getRepresentationClass())) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return defaultHandler.canImport(support);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                try {
                    Transferable t = support.getTransferable();
                    for (DataFlavor flavor : t.getTransferDataFlavors()) {
                        if (flavor.isFlavorJavaFileListType()) {
                            List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                            for (File file : files) {
                                callback.accept(FileUtil.toFileObject(file));
                            }
                            return true;
                        }
                        if (DataObject[].class.equals(flavor.getRepresentationClass())) {
                            DataObject[] dataObjects = (DataObject[]) t.getTransferData(flavor);
                            for (DataObject dobj : dataObjects) {
                                if (dobj != null) {
                                    callback.accept(dobj.getPrimaryFile());
                                }
                            }
                            return true;
                        }
                        if (Node.class.isAssignableFrom(flavor.getRepresentationClass())) {
                            Node node = (Node) t.getTransferData(flavor);
                            DataObject dobj = node.getLookup().lookup(DataObject.class);
                            if (dobj != null) {
                               callback.accept(dobj.getPrimaryFile());
                            }
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return defaultHandler.importData(support);
            }

            @Override
            public void exportToClipboard(JComponent comp, Clipboard clipboard, int action) throws IllegalStateException {
                defaultHandler.exportToClipboard(comp, clipboard, action);
            }
        });
    }
}
