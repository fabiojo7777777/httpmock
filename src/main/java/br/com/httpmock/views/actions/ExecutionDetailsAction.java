/*
 * ====================================================================
 */
package br.com.httpmock.views.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import br.com.httpmock.views.NetworkView;

/**
 *
 * Details of http request action on main window.
 *
 *
 * @since 1.0.0
 */
public class ExecutionDetailsAction implements ActionListener
{

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        NetworkView.getInstance().toggleExecutionDetails();
    }
}
