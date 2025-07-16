package com.university.academic.views;

import com.university.academic.model.entity.Mahasiswa;
import com.university.academic.model.enums.StatusMahasiswa;
import com.university.academic.model.enums.Role;
import com.university.academic.service.PersonService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "mahasiswa", layout = MainLayout.class)
@PageTitle("Mahasiswa | Sistem Informasi Akademik")
@RolesAllowed({"ADMIN", "STAFF"})
public class MahasiswaView extends VerticalLayout {

    private final PersonService personService;
    private final Grid<Mahasiswa> grid;
    private final Binder<Mahasiswa> binder;

    // Form fields
    private TextField nama;
    private TextField nim;
    private EmailField email;
    private TextField telepon;
    private TextField alamat;
    private DatePicker tanggalLahir;
    private ComboBox<String> jenisKelamin;
    private TextField jurusan;
    private IntegerField semester;
    private ComboBox<StatusMahasiswa> statusMahasiswa;
    private PasswordField password;

    private Dialog formDialog;
    private Mahasiswa currentMahasiswa;

    public MahasiswaView(PersonService personService) {
        this.personService = personService;
        this.grid = new Grid<>(Mahasiswa.class, false);
        this.binder = new Binder<>(Mahasiswa.class);

        setSizeFull();
        setSpacing(true);
        setPadding(true);
        addClassName("mahasiswa-view");

        createToolbar();
        createGrid();
        createFormDialog();
        loadData();
    }

    private void createToolbar() {
        H2 pageTitle = new H2("👥 Data Mahasiswa");
        pageTitle.getStyle()
                .set("margin", "0")
                .set("color", "#374151")
                .set("font-weight", "600");

        Button addButton = new Button("Tambah Mahasiswa", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openFormDialog(new Mahasiswa()));

        TextField searchField = new TextField();
        searchField.setPlaceholder("Cari mahasiswa...");
        searchField.addValueChangeListener(e -> filterMahasiswa(e.getValue()));
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
        grid.addColumn(Mahasiswa::getNim).setHeader("NIM").setSortable(true);
        grid.addColumn(Mahasiswa::getNama).setHeader("Nama").setSortable(true);
        grid.addColumn(Mahasiswa::getJurusan).setHeader("Jurusan").setSortable(true);
        grid.addColumn(Mahasiswa::getSemester).setHeader("Semester").setSortable(true);
        grid.addColumn(m -> m.getIpk().toString()).setHeader("IPK").setSortable(true);
        grid.addColumn(m -> m.getStatusMahasiswa().getDisplayName()).setHeader("Status").setSortable(true);

        grid.addComponentColumn(this::createActionButtons).setHeader("Aksi").setWidth("150px");

        // Simple grid styling matching dashboard
        grid.getStyle()
                .set("background", "white")
                .set("border-radius", "6px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
                .set("border", "1px solid #e5e7eb");

        add(grid);
    }

    private HorizontalLayout createActionButtons(Mahasiswa mahasiswa) {
        Button editButton = new Button(VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.addClickListener(e -> openFormDialog(mahasiswa));
        editButton.getStyle().set("color", "#059669");

        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        deleteButton.addClickListener(e -> deleteMahasiswa(mahasiswa));
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

        Button saveButton = new Button("Simpan", e -> saveMahasiswa());
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

        // Personal info fields
        nama = new TextField("Nama Lengkap");
        nim = new TextField("NIM");
        email = new EmailField("Email");
        telepon = new TextField("Telepon");
        alamat = new TextField("Alamat");
        tanggalLahir = new DatePicker("Tanggal Lahir");
        password = new PasswordField("Password");

        jenisKelamin = new ComboBox<>("Jenis Kelamin");
        jenisKelamin.setItems("L", "P");

        // Academic info fields
        jurusan = new TextField("Jurusan");
        semester = new IntegerField("Semester");
        semester.setValue(1);
        semester.setMin(1);
        semester.setMax(14);

        statusMahasiswa = new ComboBox<>("Status");
        statusMahasiswa.setItems(StatusMahasiswa.values());
        statusMahasiswa.setItemLabelGenerator(StatusMahasiswa::getDisplayName);
        statusMahasiswa.setValue(StatusMahasiswa.AKTIF);

        // Setup binder
        binder.forField(nama).asRequired("Nama wajib diisi").bind(Mahasiswa::getNama, Mahasiswa::setNama);
        binder.forField(nim).asRequired("NIM wajib diisi").bind(Mahasiswa::getNim, Mahasiswa::setNim);
        binder.forField(email).asRequired("Email wajib diisi")
                .withValidator(new EmailValidator("Format email tidak valid"))
                .bind(Mahasiswa::getEmail, Mahasiswa::setEmail);
        binder.forField(telepon).bind(Mahasiswa::getTelepon, Mahasiswa::setTelepon);
        binder.forField(alamat).bind(Mahasiswa::getAlamat, Mahasiswa::setAlamat);
        binder.forField(tanggalLahir).bind(Mahasiswa::getTanggalLahir, Mahasiswa::setTanggalLahir);
        binder.forField(jenisKelamin).bind(Mahasiswa::getJenisKelamin, Mahasiswa::setJenisKelamin);
        binder.forField(jurusan).asRequired("Jurusan wajib diisi").bind(Mahasiswa::getJurusan, Mahasiswa::setJurusan);
        binder.forField(semester).asRequired("Semester wajib diisi").bind(Mahasiswa::getSemester, Mahasiswa::setSemester);
        binder.forField(statusMahasiswa).bind(Mahasiswa::getStatusMahasiswa, Mahasiswa::setStatusMahasiswa);
        binder.forField(password).asRequired("Password wajib diisi").bind(Mahasiswa::getPassword, Mahasiswa::setPassword);

        // Set colspan for full-width fields
        formLayout.setColspan(alamat, 2);
        formLayout.setColspan(password, 2);

        formLayout.add(nama, nim, email, telepon, alamat, tanggalLahir, jenisKelamin,
                jurusan, semester, statusMahasiswa, password);

        return formLayout;
    }

    private void openFormDialog(Mahasiswa mahasiswa) {
        currentMahasiswa = mahasiswa;
        binder.setBean(mahasiswa);

        if (mahasiswa.getId() != null) {
            formDialog.setHeaderTitle("Edit Mahasiswa");
            password.setVisible(false); // Hide password field when editing
        } else {
            formDialog.setHeaderTitle("Tambah Mahasiswa");
            password.setVisible(true);
        }

        formDialog.open();
    }

    private void saveMahasiswa() {
        try {
            if (binder.validate().isOk()) {
                currentMahasiswa.setRole(Role.MAHASISWA);
                personService.saveMahasiswa(currentMahasiswa);
                loadData();
                formDialog.close();
                Notification.show("Mahasiswa berhasil disimpan", 3000, Notification.Position.TOP_CENTER);
            }
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void deleteMahasiswa(Mahasiswa mahasiswa) {
        try {
            personService.deleteById(mahasiswa.getId());
            loadData();
            Notification.show("Mahasiswa berhasil dihapus", 3000, Notification.Position.TOP_CENTER);
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void filterMahasiswa(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadData();
        } else {
            List<Mahasiswa> filtered = personService.findAllMahasiswa().stream()
                    .filter(m -> m.getNama().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            m.getNim().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            m.getJurusan().toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();
            grid.setItems(filtered);
        }
    }

    private void loadData() {
        grid.setItems(personService.findAllMahasiswa());
    }
}