package com.university.academic.views;

import com.university.academic.model.entity.Jadwal;
import com.university.academic.model.entity.MataKuliah;
import com.university.academic.model.entity.Dosen;
import com.university.academic.model.entity.Mahasiswa;
import com.university.academic.model.enums.Hari;
import com.university.academic.service.AcademicService;
import com.university.academic.service.PersonService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

@Route(value = "jadwal", layout = MainLayout.class)
@PageTitle("Jadwal | Sistem Informasi Akademik")
@RolesAllowed({"ADMIN", "STAFF", "DOSEN", "MAHASISWA"})
public class JadwalView extends VerticalLayout {

    private final AcademicService academicService;
    private final PersonService personService;
    private final AuthenticationContext authenticationContext;
    private final Grid<Jadwal> grid;
    private final Binder<Jadwal> binder;

    // Form fields untuk admin/staff
    private ComboBox<MataKuliah> mataKuliahCombo;
    private ComboBox<Dosen> dosenCombo;
    private ComboBox<Hari> hariCombo;
    private TimePicker jamMulai;
    private TimePicker jamSelesai;
    private TextField ruangan;
    private TextField tahunAkademik;

    private Dialog formDialog;
    private Jadwal currentJadwal;

    public JadwalView(AcademicService academicService, PersonService personService,
                      AuthenticationContext authenticationContext) {
        this.academicService = academicService;
        this.personService = personService;
        this.authenticationContext = authenticationContext;
        this.grid = new Grid<>(Jadwal.class, false);
        this.binder = new Binder<>(Jadwal.class);

        setSizeFull();
        setSpacing(true);
        setPadding(true);
        addClassName("jadwal-view");

        String userRole = getCurrentUserRole();
        createRoleBasedView(userRole);
    }

    private void createRoleBasedView(String userRole) {
        switch (userRole) {
            case "ROLE_MAHASISWA" -> createMahasiswaView();
            case "ROLE_DOSEN" -> createDosenView();
            case "ROLE_ADMIN", "ROLE_STAFF" -> createAdminStaffView();
            default -> {
                Paragraph noAccess = new Paragraph("Akses tidak diizinkan");
                noAccess.getStyle()
                        .set("color", "#DC2626")
                        .set("text-align", "center")
                        .set("padding", "2rem");
                add(noAccess);
            }
        }
    }

    // View untuk MAHASISWA - Read only jadwal kuliah mereka
    private void createMahasiswaView() {
        H2 title = new H2("📅 Jadwal Kuliah Saya");
        title.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "#374151")
                .set("font-weight", "600");
        add(title);

        // Get current mahasiswa
        String currentEmail = getCurrentUserEmail();
        Optional<Mahasiswa> currentMahasiswa = personService.findByEmail(currentEmail)
                .filter(p -> p instanceof Mahasiswa)
                .map(p -> (Mahasiswa) p);

        if (currentMahasiswa.isEmpty()) {
            Paragraph noData = new Paragraph("Data mahasiswa tidak ditemukan.");
            styleInfoCard(noData, "#FEF3C7", "#F59E0B", "#92400E");
            add(noData);
            return;
        }

        createMahasiswaGrid();
        loadMahasiswaJadwal(currentMahasiswa.get());

        Paragraph info = new Paragraph("💡 Jadwal ini menampilkan mata kuliah yang Anda ambil pada semester ini.");
        styleInfoCard(info, "#FEF3C7", "#F59E0B", "#92400E");
        add(info);
    }

    // View untuk DOSEN - Jadwal mengajar mereka
    private void createDosenView() {
        H2 title = new H2("🎓 Jadwal Mengajar");
        title.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "#374151")
                .set("font-weight", "600");
        add(title);

        String currentEmail = getCurrentUserEmail();
        Optional<Dosen> currentDosen = personService.findByEmail(currentEmail)
                .filter(p -> p instanceof Dosen)
                .map(p -> (Dosen) p);

        if (currentDosen.isEmpty()) {
            Paragraph noData = new Paragraph("Data dosen tidak ditemukan.");
            styleInfoCard(noData, "#FEF2F2", "#F87171", "#DC2626");
            add(noData);
            return;
        }

        // Filter section
        VerticalLayout filterSection = new VerticalLayout();
        filterSection.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("padding", "1.5rem")
                .set("margin-bottom", "1.5rem")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
                .set("border", "1px solid #e5e7eb");

        ComboBox<Hari> filterHari = new ComboBox<>("Filter Hari");
        filterHari.setItems(Hari.values());
        filterHari.setItemLabelGenerator(Hari::getDisplayName);
        filterHari.addValueChangeListener(e -> filterDosenJadwalByHari(currentDosen.get(), e.getValue()));

        filterSection.add(new H3("Filter"), filterHari);
        add(filterSection);

        createDosenGrid();
        loadDosenJadwal(currentDosen.get());

        Paragraph info = new Paragraph("📚 Ini adalah jadwal mata kuliah yang Anda ajar.");
        styleInfoCard(info, "#FEF3C7", "#F59E0B", "#92400E");
        add(info);
    }

    // View untuk ADMIN/STAFF - Full management
    private void createAdminStaffView() {
        createToolbar();
        createAdminGrid();
        createFormDialog();
        loadData();
    }

    private void createMahasiswaGrid() {
        grid.removeAllColumns();
        grid.addColumn(jadwal -> jadwal.getMataKuliah().getKodeMK()).setHeader("Kode MK");
        grid.addColumn(jadwal -> jadwal.getMataKuliah().getNamaMK()).setHeader("Mata Kuliah");
        grid.addColumn(jadwal -> jadwal.getDosen().getNama()).setHeader("Dosen");
        grid.addColumn(jadwal -> jadwal.getHari().getDisplayName()).setHeader("Hari");
        grid.addColumn(jadwal -> jadwal.getJamMulai() + " - " + jadwal.getJamSelesai()).setHeader("Waktu");
        grid.addColumn(Jadwal::getRuangan).setHeader("Ruangan");
        grid.addColumn(jadwal -> jadwal.getMataKuliah().getSks() + " SKS").setHeader("SKS");

        styleGrid();
        add(grid);
    }

    private void createDosenGrid() {
        grid.removeAllColumns();
        grid.addColumn(jadwal -> jadwal.getMataKuliah().getKodeMK()).setHeader("Kode MK");
        grid.addColumn(jadwal -> jadwal.getMataKuliah().getNamaMK()).setHeader("Mata Kuliah");
        grid.addColumn(jadwal -> jadwal.getHari().getDisplayName()).setHeader("Hari");
        grid.addColumn(jadwal -> jadwal.getJamMulai() + " - " + jadwal.getJamSelesai()).setHeader("Waktu");
        grid.addColumn(Jadwal::getRuangan).setHeader("Ruangan");
        grid.addColumn(jadwal -> jadwal.getMataKuliah().getSks() + " SKS").setHeader("SKS");
        grid.addColumn(Jadwal::getTahunAkademik).setHeader("Tahun Akademik");

        styleGrid();
        add(grid);
    }

    private void createAdminGrid() {
        grid.removeAllColumns();
        grid.addColumn(jadwal -> jadwal.getMataKuliah().getKodeMK()).setHeader("Kode MK").setSortable(true);
        grid.addColumn(jadwal -> jadwal.getMataKuliah().getNamaMK()).setHeader("Mata Kuliah").setSortable(true);
        grid.addColumn(jadwal -> jadwal.getDosen().getNama()).setHeader("Dosen").setSortable(true);
        grid.addColumn(jadwal -> jadwal.getHari().getDisplayName()).setHeader("Hari").setSortable(true);
        grid.addColumn(jadwal -> jadwal.getJamMulai().toString()).setHeader("Jam Mulai").setSortable(true);
        grid.addColumn(jadwal -> jadwal.getJamSelesai().toString()).setHeader("Jam Selesai").setSortable(true);
        grid.addColumn(Jadwal::getRuangan).setHeader("Ruangan").setSortable(true);
        grid.addColumn(Jadwal::getTahunAkademik).setHeader("Tahun Akademik").setSortable(true);

        grid.addComponentColumn(this::createActionButtons).setHeader("Aksi").setWidth("150px");

        styleGrid();
        add(grid);
    }

    private void createToolbar() {
        H2 pageTitle = new H2("🗓️ Manajemen Jadwal");
        pageTitle.getStyle()
                .set("margin", "0")
                .set("color", "#374151")
                .set("font-weight", "600");

        Button addButton = new Button("Tambah Jadwal", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openFormDialog(new Jadwal()));

        ComboBox<Hari> filterHari = new ComboBox<>("Filter Hari");
        filterHari.setItems(Hari.values());
        filterHari.setItemLabelGenerator(Hari::getDisplayName);
        filterHari.addValueChangeListener(e -> filterByHari(e.getValue()));

        HorizontalLayout toolbar = new HorizontalLayout(pageTitle, addButton, filterHari);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.setSpacing(true);

        // Simple clean styling
        toolbar.getStyle()
                .set("background", "white")
                .set("padding", "1.5rem")
                .set("border-radius", "8px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
                .set("margin-bottom", "1.5rem")
                .set("border", "1px solid #e5e7eb");

        add(toolbar);
    }

    private HorizontalLayout createActionButtons(Jadwal jadwal) {
        Button editButton = new Button(VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.addClickListener(e -> openFormDialog(jadwal));
        editButton.getStyle().set("color", "#059669");

        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        deleteButton.addClickListener(e -> deleteJadwal(jadwal));
        deleteButton.getStyle().set("color", "#DC2626");

        HorizontalLayout buttonLayout = new HorizontalLayout(editButton, deleteButton);
        buttonLayout.setSpacing(false);
        return buttonLayout;
    }

    private void styleGrid() {
        grid.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
                .set("border", "1px solid #e5e7eb");
    }

    private void styleInfoCard(Paragraph paragraph, String bgColor, String borderColor, String textColor) {
        paragraph.getStyle()
                .set("background", bgColor)
                .set("color", textColor)
                .set("padding", "1rem")
                .set("border-radius", "6px")
                .set("border-left", "3px solid " + borderColor)
                .set("margin", "1rem 0")
                .set("font-weight", "500");
    }

    private void loadMahasiswaJadwal(Mahasiswa mahasiswa) {
        // TODO: Implement method to get jadwal for specific mahasiswa
        // For now, show sample data
        List<Jadwal> sampleJadwal = academicService.findAllJadwal().stream()
                .limit(5) // Show first 5 for demo
                .toList();
        grid.setItems(sampleJadwal);
    }

    private void loadDosenJadwal(Dosen dosen) {
        List<Jadwal> dosenJadwal = academicService.findAllJadwal().stream()
                .filter(j -> j.getDosen().getId().equals(dosen.getId()))
                .toList();
        grid.setItems(dosenJadwal);
    }

    private void filterDosenJadwalByHari(Dosen dosen, Hari hari) {
        List<Jadwal> filtered = academicService.findAllJadwal().stream()
                .filter(j -> j.getDosen().getId().equals(dosen.getId()))
                .filter(j -> hari == null || j.getHari().equals(hari))
                .toList();
        grid.setItems(filtered);
    }

    private void createFormDialog() {
        formDialog = new Dialog();
        formDialog.setWidth("600px");
        formDialog.setMaxHeight("80vh");

        // Simple dialog styling
        formDialog.getElement().getStyle()
                .set("border-radius", "8px");

        FormLayout formLayout = createFormLayout();

        Button saveButton = new Button("Simpan", e -> saveJadwal());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Batal", e -> formDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.END);
        buttonLayout.setSpacing(true);
        buttonLayout.getStyle()
                .set("padding-top", "1rem")
                .set("border-top", "1px solid #e5e7eb")
                .set("margin-top", "1rem");

        VerticalLayout dialogContent = new VerticalLayout(formLayout, buttonLayout);
        dialogContent.setPadding(true);
        dialogContent.setSpacing(false);

        formDialog.add(dialogContent);
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        mataKuliahCombo = new ComboBox<>("Mata Kuliah");
        mataKuliahCombo.setItems(academicService.findAllMataKuliah());
        mataKuliahCombo.setItemLabelGenerator(MataKuliah::toString);

        dosenCombo = new ComboBox<>("Dosen");
        dosenCombo.setItems(personService.findAllDosen());
        dosenCombo.setItemLabelGenerator(Dosen::getDisplayInfo);

        hariCombo = new ComboBox<>("Hari");
        hariCombo.setItems(Hari.values());
        hariCombo.setItemLabelGenerator(Hari::getDisplayName);

        jamMulai = new TimePicker("Jam Mulai");
        jamSelesai = new TimePicker("Jam Selesai");
        ruangan = new TextField("Ruangan");
        tahunAkademik = new TextField("Tahun Akademik");
        tahunAkademik.setPlaceholder("2023/2024");

        // Setup binder
        binder.forField(mataKuliahCombo).asRequired("Mata kuliah wajib dipilih")
                .bind(Jadwal::getMataKuliah, Jadwal::setMataKuliah);
        binder.forField(dosenCombo).asRequired("Dosen wajib dipilih")
                .bind(Jadwal::getDosen, Jadwal::setDosen);
        binder.forField(hariCombo).asRequired("Hari wajib dipilih")
                .bind(Jadwal::getHari, Jadwal::setHari);
        binder.forField(jamMulai).asRequired("Jam mulai wajib diisi")
                .bind(Jadwal::getJamMulai, Jadwal::setJamMulai);
        binder.forField(jamSelesai).asRequired("Jam selesai wajib diisi")
                .bind(Jadwal::getJamSelesai, Jadwal::setJamSelesai);
        binder.forField(ruangan).asRequired("Ruangan wajib diisi")
                .bind(Jadwal::getRuangan, Jadwal::setRuangan);
        binder.forField(tahunAkademik).bind(Jadwal::getTahunAkademik, Jadwal::setTahunAkademik);

        // Set colspan for wider fields
        formLayout.setColspan(tahunAkademik, 2);

        formLayout.add(mataKuliahCombo, dosenCombo, hariCombo, jamMulai,
                jamSelesai, ruangan, tahunAkademik);

        return formLayout;
    }

    private void openFormDialog(Jadwal jadwal) {
        currentJadwal = jadwal;
        binder.setBean(jadwal);

        if (jadwal.getId() != null) {
            formDialog.setHeaderTitle("Edit Jadwal");
        } else {
            formDialog.setHeaderTitle("Tambah Jadwal");
        }

        formDialog.open();
    }

    private void saveJadwal() {
        try {
            if (binder.validate().isOk()) {
                academicService.saveJadwal(currentJadwal);
                loadData();
                formDialog.close();
                Notification.show("Jadwal berhasil disimpan", 3000, Notification.Position.TOP_CENTER);
            }
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void deleteJadwal(Jadwal jadwal) {
        try {
            academicService.deleteJadwal(jadwal.getId());
            loadData();
            Notification.show("Jadwal berhasil dihapus", 3000, Notification.Position.TOP_CENTER);
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void filterByHari(Hari hari) {
        if (hari == null) {
            loadData();
        } else {
            List<Jadwal> filtered = academicService.findAllJadwal().stream()
                    .filter(j -> j.getHari().equals(hari))
                    .toList();
            grid.setItems(filtered);
        }
    }

    private void loadData() {
        grid.setItems(academicService.findAllJadwal());
    }

    private String getCurrentUserRole() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(user -> user.getAuthorities().iterator().next().getAuthority())
                .orElse("ROLE_GUEST");
    }

    private String getCurrentUserEmail() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElse("");
    }
}