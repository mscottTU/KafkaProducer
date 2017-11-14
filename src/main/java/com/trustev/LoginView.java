package com.trustev;

import com.vaadin.event.ShortcutAction;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.io.IOException;
import java.net.URISyntaxException;

public class LoginView extends VerticalLayout implements View {
    private PropertyHelper propertyHelper;

    private Navigator nav;
    private PasswordField pfPwd;
    private TextField tfUsr;
    private Button btLogin;
    private VerticalLayout vlFields;

    public LoginView() {
        super();
        this.setSizeFull();

        makeComponents();
        setupComponents();
    }

    private void setupComponents() {
        this.setSpacing(true);
        this.addComponents(vlFields, btLogin);
        this.setComponentAlignment(vlFields, Alignment.BOTTOM_CENTER);
        this.setComponentAlignment(btLogin, Alignment.TOP_CENTER);
        vlFields.setComponentAlignment(tfUsr, Alignment.BOTTOM_CENTER);
        vlFields.setComponentAlignment(pfPwd, Alignment.BOTTOM_CENTER);

        btLogin.addClickListener(e->login());
        btLogin.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        tfUsr.focus();
    }

    private void login() {
        if (validateCredentials()){
            nav.navigateTo("Kakfa Publisher");
        } else {
            Notification notifcation = new Notification("Login Failed", "Check username and password, then try again", Notification.Type.WARNING_MESSAGE);
            notifcation.show(Page.getCurrent());
        }
    }

    private boolean validateCredentials() {
        String name = tfUsr.getValue();
        String pass = pfPwd.getValue();

        if (name.equals(propertyHelper.loadProperty("user.name").get(0)) && pass.equals(propertyHelper.loadProperty("user.pwd").get(0))){
            return true;
        } else {
            return false;
        }
    }

    private void makeComponents() {
        pfPwd = new PasswordField("Password:");
        pfPwd.setIcon(VaadinIcons.LOCK);
        tfUsr = new TextField("Username:");
        tfUsr.setIcon(VaadinIcons.USER);
        btLogin = new Button("Login", VaadinIcons.ARROW_FORWARD);
        btLogin.setStyleName(ValoTheme.BUTTON_PRIMARY);
        ThemeResource themeResource = new ThemeResource("trustev-logo-white.png");
        Image image = new Image("", themeResource);
        vlFields = new VerticalLayout(image, tfUsr, pfPwd);
        vlFields.setSpacing(true);

        vlFields.setComponentAlignment(image, Alignment.TOP_CENTER);

        try {
            propertyHelper = new PropertyHelper("kafka.properties");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setNavigator(Navigator navigator) {
        this.nav = navigator;
    }
}
