package com.university.academic.views;

import com.university.academic.model.entity.MataKuliah;
import com.university.academic.service.AcademicService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Route(value = "matakuliah", layout = MainLayout.class)
@PageTitle("Mata Kuliah | Sistem Informasi Akademik")
@RolesAllowed({"ADMIN", "STAFF", "DOSEN"})
public class MataKuliahView extends VerticalLayout {

    private final AcademicService academicService;
    private final AuthenticationContext authenticationContext;
    private final Grid<MataKuliah> grid;
    private final Binder<MataKuliah> binder;

    // Form fields
    private TextField kodeMK;
    private TextField namaMK;
    private IntegerField sks;
    private IntegerField semester;
    private TextField fakultas;
    private TextArea deskripsi;
    private TextField prerequisite;

    private Dialog formDialog;
    private MataKuliah currentMataKuliah;

    public MataKuliahView(AcademicService academicService, AuthenticationContext authenticationContext) {
        this.academicService = academicService;
        this.authenticationContext = authenticationContext;
        this.grid = new Grid<>(MataKuliah.class, false);
        this.binder = new Binder<>(MataKuliah.class);

        setSizeFull();
        setSpacing(true);
        setPadding(true);
        addClassName("matakuliah-view");

        // Cek role untuk tampilan berbeda
        String userRole = getCurrentUserRole();
        if ("ROLE_DOSEN".equals(userRole)) {
            createDosenView();
        } else {
            createAdminStaffView();
        }
    }

    // VIEW UNTUK DOSEN - Read Only
    private void createDosenView() {
        H2 title = new H2("📚 Data Mata Kuliah");
        title.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("color", "#374151")
                .set("font-weight", "600");
        add(title);

        Paragraph info = new Paragraph("📖 Sebagai dosen, Anda dapat melihat informasi mata kuliah yang tersedia.");
        styleInfoCard(info);
        add(info);

        // Search section
        VerticalLayout searchSection = new VerticalLayout();
        searchSection.getStyle()
                .set("background", "white")
                .set("border-radius", "6px")
                .set("padding", "1rem")
                .set("margin-bottom", "1.5rem")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
                .set("border", "1px solid #e5e7eb");

        TextField searchField = new TextField("Cari Mata Kuliah");
        searchField.setPlaceholder("Cari mata kuliah...");
        searchField.addValueChangeListener(e -> filterMataKuliah(e.getValue()));
        searchField.setWidthFull();

        searchSection.add(searchField);
        add(searchSection);

        createDosenGrid();
        loadData();
    }

    // VIEW UNTUK ADMIN/STAFF - Full Management
    private void createAdminStaffView() {
        createToolbar();
        createGrid();
        createFormDialog();
        loadData();
    }

    private void createDosenGrid() {
        grid.addColumn(MataKuliah::getKodeMK).setHeader("Kode MK").setSortable(true).setWidth("120px");
        grid.addColumn(MataKuliah::getNamaMK).setHeader("Nama Mata Kuliah").setSortable(true);
        grid.addColumn(MataKuliah::getSks).setHeader("SKS").setSortable(true).setWidth("80px");
        grid.addColumn(MataKuliah::getSemester).setHeader("Semester").setSortable(true).setWidth("100px");
        grid.addColumn(MataKuliah::getFakultas).setHeader("Fakultas").setSortable(true);
        grid.addColumn(MataKuliah::getDeskripsi).setHeader("Deskripsi");
        grid.addColumn(MataKuliah::getPrerequisite).setHeader("Prerequisite");

        styleGrid();
        add(grid);
    }

    private void createToolbar() {
        H2 pageTitle = new H2("📚 Data Mata Kuliah");
        pageTitle.getStyle()
                .set("margin", "0")
                .set("color", "#374151")
                .set("font-weight", "600");

        Button addButton = new Button("Tambah Mata Kuliah", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openFormDialog(new MataKuliah()));

        TextField searchField = new TextField();
        searchField.setPlaceholder("Cari mata kuliah...");
        searchField.addValueChangeListener(e -> filterMataKuliah(e.getValue()));
        searchField.getStyle()
                .set("min-width", "250px");

        HorizontalLayout toolbar = new HorizontalLayout(pageTitle, addButton, searchField);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(Alignment.CENTER);
        toolbar.setSpacing(true);

        // Simple clean styling matching dashboard
        toolbar.getStyle()
                .set("background", "white")
                .set("padding", "1.5rem")
                .set("border-radius", "6px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
                .set("margin-bottom", "1.5rem")
                .set("border", "1px solid #e5e7eb");

        add(toolbar);
    }

    private void createGrid() {
        grid.addColumn(MataKuliah::getKodeMK).setHeader("Kode MK").setSortable(true).setWidth("120px");
        grid.addColumn(MataKuliah::getNamaMK).setHeader("Nama Mata Kuliah").setSortable(true);
        grid.addColumn(MataKuliah::getSks).setHeader("SKS").setSortable(true).setWidth("80px");
        grid.addColumn(MataKuliah::getSemester).setHeader("Semester").setSortable(true).setWidth("100px");
        grid.addColumn(MataKuliah::getFakultas).setHeader("Fakultas").setSortable(true);

        grid.addComponentColumn(this::createActionButtons).setHeader("Aksi").setWidth("150px");

        styleGrid();
        add(grid);
    }

    private HorizontalLayout createActionButtons(MataKuliah mataKuliah) {
        Button editButton = new Button(VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.addClickListener(e -> openFormDialog(mataKuliah));
        editButton.getStyle().set("color", "#059669");

        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        deleteButton.addClickListener(e -> deleteMataKuliah(mataKuliah));
        deleteButton.getStyle().set("color", "#DC2626");

        HorizontalLayout buttonLayout = new HorizontalLayout(editButton, deleteButton);
        buttonLayout.setSpacing(false);
        return buttonLayout;
    }

    private void createFormDialog() {
        formDialog = new Dialog();
        formDialog.setWidth("600px");
        formDialog.setMaxHeight("80vh");

        // Simple dialog styling
        formDialog.getElement().getStyle()
                .set("border-radius", "6px");

        FormLayout formLayout = createFormLayout();

        Button saveButton = new Button("Simpan", e -> saveMataKuliah());
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

        kodeMK = new TextField("Kode Mata Kuliah");
        namaMK = new TextField("Nama Mata Kuliah");
        sks = new IntegerField("SKS");
        sks.setMin(1);
        sks.setMax(6);
        sks.setValue(3);

        semester = new IntegerField("Semester");
        semester.setMin(1);
        semester.setMax(8);
        semester.setValue(1);

        fakultas = new TextField("Fakultas");
        deskripsi = new TextArea("Deskripsi");
        deskripsi.setHeight("100px");

        prerequisite = new TextField("Prerequisite");
        prerequisite.setHelperText("Masukkan kode mata kuliah prerequisite, dipisah koma");

        // Setup binder
        binder.forField(kodeMK).asRequired("Kode mata kuliah wajib diisi")
                .bind(MataKuliah::getKodeMK, MataKuliah::setKodeMK);
        binder.forField(namaMK).asRequired("Nama mata kuliah wajib diisi")
                .bind(MataKuliah::getNamaMK, MataKuliah::setNamaMK);
        binder.forField(sks).asRequired("SKS wajib diisi")
                .bind(MataKuliah::getSks, MataKuliah::setSks);
        binder.forField(semester).asRequired("Semester wajib diisi")
                .bind(MataKuliah::getSemester, MataKuliah::setSemester);
        binder.forField(fakultas).asRequired("Fakultas wajib diisi")
                .bind(MataKuliah::getFakultas, MataKuliah::setFakultas);
        binder.forField(deskripsi).bind(MataKuliah::getDeskripsi, MataKuliah::setDeskripsi);

        // prerequisite binding dengan null representation
        binder.forField(prerequisite)
                .withNullRepresentation("")
                .bind(MataKuliah::getPrerequisite, MataKuliah::setPrerequisite);

        // Set colspan for full-width fields
        formLayout.setColspan(namaMK, 2);
        formLayout.setColspan(deskripsi, 2);
        formLayout.setColspan(prerequisite, 2);

        formLayout.add(kodeMK, namaMK, sks, semester, fakultas, deskripsi, prerequisite);

        return formLayout;
    }

    private void styleGrid() {
        grid.getStyle()
                .set("background", "white")
                .set("border-radius", "6px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
                .set("border", "1px solid #e5e7eb");
    }

    private void styleInfoCard(Paragraph paragraph) {
        paragraph.getStyle()
                .set("background", "#FEF3C7")
                .set("color", "#92400E")
                .set("padding", "1rem")
                .set("border-radius", "6px")
                .set("border-left", "3px solid #F59E0B")
                .set("margin", "1rem 0")
                .set("font-weight", "500")
                .set("font-size", "0.9rem");
    }

    private void openFormDialog(MataKuliah mataKuliah) {
        currentMataKuliah = mataKuliah;
        binder.setBean(mataKuliah);

        if (mataKuliah.getKodeMK() != null) {
            formDialog.setHeaderTitle("Edit Mata Kuliah");
            kodeMK.setReadOnly(true); // Prevent editing primary key
        } else {
            formDialog.setHeaderTitle("Tambah Mata Kuliah");
            kodeMK.setReadOnly(false);
        }

        formDialog.open();
    }

    private void saveMataKuliah() {
        try {
            // VALIDATE & COPY data dari form ke currentMataKuliah
            if (binder.writeBeanIfValid(currentMataKuliah)) {
                academicService.saveMataKuliah(currentMataKuliah);
                loadData();
                formDialog.close();
                Notification.show("Mata kuliah berhasil disimpan", 3000, Notification.Position.TOP_CENTER);
            } else {
                Notification.show("Validasi gagal. Cek kembali input Anda.", 3000, Notification.Position.TOP_CENTER);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Error: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void deleteMataKuliah(MataKuliah mataKuliah) {
        try {
            academicService.deleteMataKuliah(mataKuliah.getKodeMK());
            loadData();
            Notification.show("Mata kuliah berhasil dihapus", 3000, Notification.Position.TOP_CENTER);
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void filterMataKuliah(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadData();
        } else {
            List<MataKuliah> filtered = academicService.findAllMataKuliah().stream()
                    .filter(mk -> mk.getKodeMK().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            mk.getNamaMK().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            mk.getFakultas().toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();
            grid.setItems(filtered);
        }
    }

    private void loadData() {
        grid.setItems(academicService.findAllMataKuliah());
    }

    private String getCurrentUserRole() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(user -> user.getAuthorities().iterator().next().getAuthority())
                .orElse("ROLE_GUEST");
    }
}