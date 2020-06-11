package br.com.httpmock.views.actions;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import br.com.httpmock.views.NetworkView;

public class ClickOnCellAction implements ListSelectionListener
{

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        NetworkView.getInstance().detail();
    }
}