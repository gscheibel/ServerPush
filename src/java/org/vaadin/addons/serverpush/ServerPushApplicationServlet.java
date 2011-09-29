
/*
 * Copyright (C) 2011 Elihu, LLC. All rights reserved.
 *
 * $Id$
 */

package org.vaadin.addons.serverpush;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationServlet;
import com.vaadin.terminal.gwt.server.WebApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * {@link ApplicationServlet} that uses its own {@link WebApplicationContext} to provide {@link ServerPushCommunicationManager}
 * which pushes updates to all client on every repaint request
 */
public class ServerPushApplicationServlet extends ApplicationServlet {

    @Override
    protected Application getNewApplication(final HttpServletRequest request) throws ServletException {
        final Application application = super.getNewApplication(request);
        if (application != null) {
            application.addListener(new Application.WindowAttachListener() {
                public void windowAttached(final Application.WindowAttachEvent event) {
                    new Thread() {
                        public void run() {
                            while (event.getApplication().getMainWindow() == null) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    // ignore
                                }
                            }
                            event.getApplication().getMainWindow().addComponent(new ServerPush(request.getContextPath()));
                        }
                    }.start();
                }
            });
        }
        return application;
    }

    @Override
    protected WebApplicationContext getApplicationContext(HttpSession session) {
        WebApplicationContext cx = (WebApplicationContext)session.getAttribute(WebApplicationContext.class.getName());
        if (cx == null) {
            cx = new ServerPushWebApplicationContext(session);
            session.setAttribute(WebApplicationContext.class.getName(), cx);
        }
        return cx;
    }
}
