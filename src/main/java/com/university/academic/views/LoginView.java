package com.university.academic.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.html.Paragraph;

@Route("login")
@PageTitle("Login | Sistem Informasi Akademik")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Konfigurasi LoginForm
        login.setAction("login");
        login.setForgotPasswordButtonVisible(false);

        // Header
        H1 title = new H1("Sistem Informasi Akademik");
        title.getStyle().set("color", "white");
        title.getStyle().set("text-align", "center");
        title.getStyle().set("margin-bottom", "2rem");
        title.getStyle().set("font-size", "2.5rem");
        title.getStyle().set("text-shadow", "2px 2px 4px rgba(0,0,0,0.3)");

        // Info untuk testing
//        Paragraph info = new Paragraph("Demo Login: admin@university.ac.id / password123");
//        info.getStyle().set("color", "white");
//        info.getStyle().set("text-align", "center");
//        info.getStyle().set("background", "rgba(255,255,255,0.2)");
//        info.getStyle().set("padding", "1rem");
//        info.getStyle().set("border-radius", "8px");
//        info.getStyle().set("margin-bottom", "2rem");
//        info.getStyle().set("backdrop-filter", "blur(10px)");

        // Container untuk form
        Div loginContainer = new Div();
        loginContainer.add(login);
        loginContainer.getStyle().set("background", "white");
        loginContainer.getStyle().set("padding", "2rem");
        loginContainer.getStyle().set("border-radius", "12px");
        loginContainer.getStyle().set("box-shadow", "0 8px 32px rgba(0, 0, 0, 0.2)");
        loginContainer.getStyle().set("max-width", "400px");
        loginContainer.getStyle().set("width", "100%");
        loginContainer.getStyle().set("backdrop-filter", "blur(10px)");

        // Background styling
        getStyle().set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");
        getStyle().set("min-height", "100vh");

        add(title, loginContainer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // Cek jika ada parameter error
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
            Notification.show("Login gagal! Periksa email dan password Anda.",
                    5000, Notification.Position.TOP_CENTER);
        }

        // Cek jika ada parameter logout
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("logout")) {
            Notification.show("Logout berhasil!",
                    3000, Notification.Position.TOP_CENTER);
        }
    }
}