/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
