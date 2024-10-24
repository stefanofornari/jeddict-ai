/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.github.jeddict.ai.actions;

import static javax.swing.Action.NAME;
import org.openide.util.NbBundle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

@ActionID(
        category = "Edit/Chat",
        id = "io.github.jeddict.ai.actions.AIAssistantPopupAction"
)
@ActionRegistration(
        displayName = "#CTL_AIAssistantPopupAction", lazy = false
)
@ActionReference(path = "Editors/Popup", position = 101)
@NbBundle.Messages("CTL_AIAssistantPopupAction=AI Assistant")
public final class AIAssistantPopupAction extends AbstractAction implements ActionListener, Presenter.Popup {

    public AIAssistantPopupAction() {
        putValue(NAME, Bundle.CTL_AIAssistantPopupAction());
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public JMenuItem getPopupPresenter() {
        setEnabled(true);
        JMenu main = new JMenu(this);
        List<? extends Action> actionsForPath = Utilities.actionsForPath("Actions/Chat/SubActions");
        actionsForPath.forEach((action) -> {
            main.add(action);
        });
        return main;
    }

}
