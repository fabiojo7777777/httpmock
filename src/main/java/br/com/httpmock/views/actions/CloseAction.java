package br.com.httpmock.views.actions;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;

public class CloseAction extends WindowAdapter
{
    @Override
    public void windowClosing(WindowEvent e)
    {
        if (JOptionPane.showConfirmDialog(null, "Fechar aplicação?", "Fechar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
        {
            System.exit(0);
        }
    }
}
