package br.com.httpmock.views.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import br.com.httpmock.views.NetworkView;

public class ClearAction implements ActionListener
{

    @Override
    public void actionPerformed(ActionEvent e)
    {
        NetworkView.getInstance().clear();
    }

}
