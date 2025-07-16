package com.university.academic.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public class MainLayout extends AppLayout {

    private final AuthenticationContext authenticationContext;

    // PERBAIKAN: Inject via constructor, bukan @Autowired
    public MainLayout(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();

        H1 viewTitle = new H1("Sistem Informasi Akademik");
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        Button logoutButton = new Button("Logout", VaadinIcon.SIGN_OUT.create());
        logoutButton.addClickListener(e -> logout());

        Header header = new Header(toggle, viewTitle, logoutButton);
        header.addClassNames(LumoUtility.AlignItems.CENTER, LumoUtility.Display.FLEX,
                LumoUtility.JustifyContent.BETWEEN, LumoUtility.Width.FULL);

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();

        // Dashboard - available for all roles
        nav.addItem(new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD.create()));

        // Get current user role
        String userRole = getCurrentUserRole();

        // PERBAIKAN: Add debug log to see what role is detected
        System.out.println("DEBUG: Current user role = " + userRole);

        // ADMIN - Full access
        if ("ROLE_ADMIN".equals(userRole)) {
            nav.addItem(new SideNavItem("Mahasiswa", MahasiswaView.class, VaadinIcon.USERS.create()));
            nav.addItem(new SideNavItem("Dosen", DosenView.class, VaadinIcon.USER.create()));
            nav.addItem(new SideNavItem("Mata Kuliah", MataKuliahView.class, VaadinIcon.BOOK.create()));
            nav.addItem(new SideNavItem("Jadwal", JadwalView.class, VaadinIcon.CALENDAR.create()));
            nav.addItem(new SideNavItem("Input Nilai", NilaiView.class, VaadinIcon.EDIT.create()));
        }

        // STAFF - Data management
        else if ("ROLE_STAFF".equals(userRole)) {
            nav.addItem(new SideNavItem("Mahasiswa", MahasiswaView.class, VaadinIcon.USERS.create()));
            nav.addItem(new SideNavItem("Dosen", DosenView.class, VaadinIcon.USER.create()));
            nav.addItem(new SideNavItem("Mata Kuliah", MataKuliahView.class, VaadinIcon.BOOK.create()));
            nav.addItem(new SideNavItem("Jadwal", JadwalView.class, VaadinIcon.CALENDAR.create()));
        }

        // DOSEN - Teaching related
        else if ("ROLE_DOSEN".equals(userRole)) {
            nav.addItem(new SideNavItem("Mata Kuliah", MataKuliahView.class, VaadinIcon.BOOK.create()));
            nav.addItem(new SideNavItem("Jadwal Mengajar", JadwalView.class, VaadinIcon.CALENDAR.create()));
            nav.addItem(new SideNavItem("Input Nilai", NilaiView.class, VaadinIcon.EDIT.create()));
        }

        // MAHASISWA - Limited view only
        else if ("ROLE_MAHASISWA".equals(userRole)) {
            System.out.println("DEBUG: Adding MAHASISWA menu items");
            nav.addItem(new SideNavItem("Jadwal Kuliah", JadwalView.class, VaadinIcon.CALENDAR.create()));
            nav.addItem(new SideNavItem("Transkrip Nilai", NilaiView.class, VaadinIcon.RECORDS.create()));
        }

        addToDrawer(new Scroller(nav));
    }

    private String getCurrentUserRole() {
        try {
            if (authenticationContext != null) {
                Optional<UserDetails> userDetails = authenticationContext.getAuthenticatedUser(UserDetails.class);
                if (userDetails.isPresent()) {
                    String role = userDetails.get().getAuthorities().iterator().next().getAuthority();
                    System.out.println("DEBUG: Found role = " + role);
                    return role;
                }
            }
            System.out.println("DEBUG: No authenticated user found");
        } catch (Exception e) {
            System.out.println("DEBUG: Error getting user role: " + e.getMessage());
        }
        return "ROLE_GUEST";
    }

    private void logout() {
        if (authenticationContext != null) {
            authenticationContext.logout();
        }
    }
}