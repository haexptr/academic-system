package com.university.academic.views;

import com.university.academic.service.ReportService;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Sistem Informasi Akademik")
@RolesAllowed({"ADMIN", "STAFF", "DOSEN", "MAHASISWA"})
public class DashboardView extends VerticalLayout {

    private final ReportService reportService;
    private final AuthenticationContext authenticationContext;

    public DashboardView(ReportService reportService, AuthenticationContext authenticationContext) {
        this.reportService = reportService;
        this.authenticationContext = authenticationContext;

        setSpacing(true);
        setPadding(true);
        setSizeFull();
        addClassName("dashboard-view");

        createDashboard();
    }

    private void createDashboard() {
        // Compact Header
        createCompactHeader();

        // Statistics (for Admin/Staff only)
        String userRole = getCurrentUserRole();
        if ("ROLE_ADMIN".equals(userRole) || "ROLE_STAFF".equals(userRole)) {
            createCompactStatistics();
        }

        // Quick Actions
        createCompactActions(userRole);

        // Tips Section
        createCompactTips(userRole);
    }

    private void createCompactHeader() {
        // Minimal Welcome Card
        Div headerCard = new Div();
        headerCard.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("color", "white")
                .set("padding", "1.25rem") // Reduced from 2rem
                .set("border-radius", "8px") // Reduced from 12px
                .set("margin-bottom", "1.5rem") // Reduced from 2rem
                .set("box-shadow", "0 2px 8px rgba(102, 126, 234, 0.3)"); // Reduced shadow

        String username = authenticationContext.getAuthenticatedUser(org.springframework.security.core.userdetails.UserDetails.class)
                .map(user -> user.getUsername())
                .orElse("User");

        String userRole = getCurrentUserRole();
        String roleName = getRoleDisplayName(userRole);

        // Compact content
        HorizontalLayout headerContent = new HorizontalLayout();
        headerContent.setWidthFull();
        headerContent.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerContent.setAlignItems(Alignment.CENTER);
        headerContent.setSpacing(true);

        // Left side - Welcome info
        VerticalLayout welcomeInfo = new VerticalLayout();
        welcomeInfo.setSpacing(false);
        welcomeInfo.setPadding(false);

        H2 welcomeTitle = new H2("👋 Selamat Datang!"); // Reduced from H1
        welcomeTitle.getStyle()
                .set("margin", "0")
                .set("font-size", "1.5rem") // Reduced from 2.5rem
                .set("font-weight", "600");

        Paragraph userInfo = new Paragraph(username + " (" + roleName + ")");
        userInfo.getStyle()
                .set("margin", "0.25rem 0 0 0") // Reduced margin
                .set("font-size", "0.95rem")
                .set("opacity", "0.9");

        welcomeInfo.add(welcomeTitle, userInfo);

        // Right side - Date/time (compact)
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph dateTime = new Paragraph("📅 " + currentDateTime);
        dateTime.getStyle()
                .set("margin", "0")
                .set("font-size", "0.85rem")
                .set("opacity", "0.8")
                .set("text-align", "right");

        headerContent.add(welcomeInfo, dateTime);
        headerCard.add(headerContent);
        add(headerCard);
    }

    private void createCompactStatistics() {
        H3 statsTitle = new H3("📊 Statistik"); // Reduced from H2
        statsTitle.getStyle().set("margin", "1rem 0 0.75rem 0"); // Reduced margins
        add(statsTitle);

        Map<String, Object> stats = reportService.getDashboardStats();
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);

        // Compact stat cards
        Div mahasiswaCard = createCompactStatCard("👥 Mahasiswa", String.valueOf(stats.get("totalMahasiswa")), "#4F46E5");
        Div mataKuliahCard = createCompactStatCard("📚 Mata Kuliah", String.valueOf(stats.get("totalMataKuliah")), "#059669");
        Div dosenCard = createCompactStatCard("🎓 Dosen", String.valueOf(stats.get("totalDosen")), "#DC2626");

        @SuppressWarnings("unchecked")
        Map<String, Long> jurusanStats = (Map<String, Long>) stats.get("statistikJurusan");
        Div jurusanCard = createCompactStatCard("🏢 Jurusan", String.valueOf(jurusanStats.size()), "#7C3AED");

        statsLayout.add(mahasiswaCard, mataKuliahCard, dosenCard, jurusanCard);
        add(statsLayout);
    }

    private Div createCompactStatCard(String title, String value, String color) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "6px") // Reduced from 12px
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)") // Reduced shadow
                .set("padding", "1rem") // Reduced from 1.5rem
                .set("margin", "0.25rem") // Reduced margin
                .set("flex", "1")
                .set("border-top", "3px solid " + color) // Reduced from 4px
                .set("transition", "transform 0.2s")
                .set("cursor", "pointer");

        // Subtle hover
        card.getElement().setAttribute("onmouseover", "this.style.transform='translateY(-2px)'");
        card.getElement().setAttribute("onmouseout", "this.style.transform='translateY(0)'");

        // Compact header
        H4 titleElement = new H4(title); // Reduced from H3
        titleElement.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("color", "#374151")
                .set("font-size", "0.8rem") // Smaller
                .set("font-weight", "500");

        // Compact value
        H2 valueElement = new H2(value); // Reduced from H1
        valueElement.getStyle()
                .set("font-size", "1.75rem") // Reduced from 2.5rem
                .set("font-weight", "700")
                .set("margin", "0")
                .set("color", color);

        card.add(titleElement, valueElement);
        return card;
    }

    private void createCompactActions(String userRole) {
        H3 actionsTitle = new H3("⚡ Aksi Cepat"); // Reduced from H2
        actionsTitle.getStyle().set("margin", "1.5rem 0 0.75rem 0"); // Reduced margins
        add(actionsTitle);

        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setWidthFull();
        actionsLayout.setSpacing(true);

        switch (userRole) {
            case "ROLE_ADMIN" -> {
                actionsLayout.add(
                        createCompactActionCard("👥 Kelola Mahasiswa", "Tambah, edit, atau hapus data mahasiswa", MahasiswaView.class, "#4F46E5"),
                        createCompactActionCard("🎓 Kelola Dosen", "Manajemen data dosen dan staff", DosenView.class, "#059669"),
                        createCompactActionCard("📚 Kelola Mata Kuliah", "Atur mata kuliah dan kurikulum", MataKuliahView.class, "#DC2626"),
                        createCompactActionCard("📝 Input Nilai", "Kelola nilai mahasiswa", NilaiView.class, "#7C3AED")
                );
            }
            case "ROLE_STAFF" -> {
                actionsLayout.add(
                        createCompactActionCard("👥 Data Mahasiswa", "Lihat dan kelola data mahasiswa", MahasiswaView.class, "#4F46E5"),
                        createCompactActionCard("📚 Data Mata Kuliah", "Kelola mata kuliah", MataKuliahView.class, "#059669"),
                        createCompactActionCard("🗓️ Jadwal Kuliah", "Atur jadwal perkuliahan", JadwalView.class, "#DC2626")
                );
            }
            case "ROLE_DOSEN" -> {
                actionsLayout.add(
                        createCompactActionCard("🗓️ Jadwal Mengajar", "Lihat jadwal mengajar Anda", JadwalView.class, "#4F46E5"),
                        createCompactActionCard("📝 Input Nilai", "Input nilai mahasiswa", NilaiView.class, "#059669"),
                        createCompactActionCard("📚 Mata Kuliah", "Lihat mata kuliah", MataKuliahView.class, "#DC2626")
                );
            }
            case "ROLE_MAHASISWA" -> {
                actionsLayout.add(
                        createCompactActionCard("🗓️ Jadwal Kuliah", "Lihat jadwal kuliah Anda", JadwalView.class, "#4F46E5"),
                        createCompactActionCard("📋 Transkrip Nilai", "Lihat nilai dan IPK", NilaiView.class, "#059669")
                );
            }
        }

        add(actionsLayout);
    }

    private Div createCompactActionCard(String title, String description, Class<?> targetView, String color) {
        Div card = new Div();
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "6px") // Reduced
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)") // Reduced
                .set("padding", "1rem") // Reduced from 1.5rem
                .set("margin", "0.25rem") // Reduced
                .set("flex", "1")
                .set("cursor", "pointer")
                .set("transition", "all 0.2s ease")
                .set("border", "1px solid #e5e7eb"); // Added subtle border

        // Hover effect
        card.getElement().setAttribute("onmouseover",
                "this.style.transform='translateY(-2px)'; this.style.borderColor='" + color + "'");
        card.getElement().setAttribute("onmouseout",
                "this.style.transform='translateY(0)'; this.style.borderColor='#e5e7eb'");

        H4 titleElement = new H4(title); // Reduced from H3
        titleElement.getStyle()
                .set("margin", "0 0 0.4rem 0") // Reduced
                .set("color", color)
                .set("font-weight", "600")
                .set("font-size", "0.95rem"); // Smaller

        Paragraph descElement = new Paragraph(description);
        descElement.getStyle()
                .set("margin", "0")
                .set("color", "#6B7280")
                .set("font-size", "0.8rem"); // Smaller

        card.add(titleElement, descElement);

        // Navigation
        card.addClickListener(e -> {
            String route = getRouteForView(targetView);
            card.getUI().ifPresent(ui -> ui.navigate(route));
        });

        return card;
    }

    private void createCompactTips(String userRole) {
        H3 infoTitle = new H3("💡 Tips & Informasi"); // Reduced from H2
        infoTitle.getStyle().set("margin", "1.5rem 0 0.75rem 0"); // Reduced
        add(infoTitle);

        Div infoCard = new Div();
        infoCard.getStyle()
                .set("background", "#FEF3C7")
                .set("border-radius", "6px") // Reduced
                .set("padding", "1rem") // Reduced from 1.5rem
                .set("border-left", "3px solid #F59E0B"); // Reduced from 4px

        String tipText = switch (userRole) {
            case "ROLE_ADMIN" -> "💼 Sebagai Administrator, pastikan untuk melakukan backup data secara berkala dan memantau aktivitas sistem.";
            case "ROLE_STAFF" -> "📋 Sebagai Staff, pastikan data mahasiswa dan mata kuliah selalu update untuk kelancaran akademik.";
            case "ROLE_DOSEN" -> "🎯 Sebagai Dosen, jangan lupa untuk input nilai tepat waktu dan cek jadwal mengajar secara rutin.";
            case "ROLE_MAHASISWA" -> "📚 Sebagai Mahasiswa, selalu cek jadwal kuliah dan pantau perkembangan nilai Anda.";
            default -> "🎓 Selamat menggunakan Sistem Informasi Akademik!";
        };

        Paragraph tipParagraph = new Paragraph(tipText);
        tipParagraph.getStyle()
                .set("margin", "0")
                .set("color", "#92400E")
                .set("font-weight", "500")
                .set("font-size", "0.9rem"); // Smaller

        infoCard.add(tipParagraph);
        add(infoCard);
    }

    // Helper methods (unchanged)
    private String getRouteForView(Class<?> viewClass) {
        if (viewClass == MahasiswaView.class) return "mahasiswa";
        if (viewClass == DosenView.class) return "dosen";
        if (viewClass == MataKuliahView.class) return "matakuliah";
        if (viewClass == JadwalView.class) return "jadwal";
        if (viewClass == NilaiView.class) return "nilai";
        return "";
    }

    private String getCurrentUserRole() {
        return authenticationContext.getAuthenticatedUser(org.springframework.security.core.userdetails.UserDetails.class)
                .map(user -> user.getAuthorities().iterator().next().getAuthority())
                .orElse("ROLE_GUEST");
    }

    private String getRoleDisplayName(String role) {
        return switch (role) {
            case "ROLE_ADMIN" -> "Administrator";
            case "ROLE_STAFF" -> "Staff";
            case "ROLE_DOSEN" -> "Dosen";
            case "ROLE_MAHASISWA" -> "Mahasiswa";
            default -> "Guest";
        };
    }
}