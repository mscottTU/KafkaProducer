package com.trustev;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

import javax.servlet.annotation.WebServlet;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class MyUI extends UI {

    private Navigator nav;
    private PublisherLayout publisherLayout;
    private LoginView loginView;

    @Override
    protected void init(VaadinRequest request) {
        getPage().setTitle("Trustev Kafka Publisher");
        publisherLayout = new PublisherLayout();
        loginView = new LoginView();
        nav =  new Navigator(this, this);
        nav.addView("Login", loginView);
        nav.addView("Kakfa Publisher", publisherLayout);
        nav.navigateTo("Login");
        loginView.setNavigator(this.nav);
    }


    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }


}
