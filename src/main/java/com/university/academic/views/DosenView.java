package com.university.academic.views;

import com.university.academic.model.entity.Dosen;
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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "dosen", layout = MainLayout.class)
@PageTitle("Dosen | Sistem Informasi Akademik")
@RolesAllowed({"ADMIN", "STAFF"})
public class DosenView extends VerticalLayout {

    private final PersonService personService;
    private final Grid<Dosen> grid;
    private final Binder<Dosen> binder;

    // Form fields
    private TextField nama;
    private TextField nip;
    private EmailField email;
    private TextField telepon;
    private TextField alamat;
    private DatePicker tanggalLahir;
    private ComboBox<String> jenisKelamin;
    private TextField jabatan;
    private TextField fakultas;
    private TextField bidangKeahlian;
    private ComboBox<String> statusDosen;
    private PasswordField password;

    private Dialog formDialog;
    private Dosen currentDosen;

    public DosenView(PersonService personService) {
        this.personService = personService;
        this.grid = new Grid<>(Dosen.class, false);
        this.binder = new Binder<>(Dosen.class);

        setSizeFull();
        setSpacing(true);
        setPadding(true);
        addClassName("dosen-view");

        createToolbar();
        createGrid();
        createFormDialog();
        loadData();
    }

    private void createToolbar() {
        H2 pageTitle = new H2("👨‍🏫 Data Dosen");
        pageTitle.getStyle()
                .set("margin", "0")
                .set("color", "#374151")
                .set("font-weight", "600");

        Button addButton = new Button("Tambah Dosen", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openFormDialog(new Dosen()));

        TextField searchField = new TextField();
        searchField.setPlaceholder("Cari dosen...");
        searchField.addValueChangeListener(e -> filterDosen(e.getValue()));
        searchField.getStyle()
                .set("min-width", "250px");

        HorizontalLayout toolbar = new HorizontalLayout(pageTitle, addButton, searchField);
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

    private void createGrid() {
        grid.addColumn(Dosen::getNip).setHeader("NIP").setSortable(true);
        grid.addColumn(Dosen::getNama).setHeader("Nama").setSortable(true);
        grid.addColumn(Dosen::getFakultas).setHeader("Fakultas").setSortable(true);
        grid.addColumn(Dosen::getJabatan).setHeader("Jabatan").setSortable(true);
        grid.addColumn(Dosen::getBidangKeahlian).setHeader("Bidang Keahlian").setSortable(true);
        grid.addColumn(Dosen::getStatusDosen).setHeader("Status").setSortable(true);

        grid.addComponentColumn(this::createActionButtons).setHeader("Aksi").setWidth("150px");

        // Simple grid styling
        grid.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)")
                .set("border", "1px solid #e5e7eb");

        add(grid);
    }

    private HorizontalLayout createActionButtons(Dosen dosen) {
        Button editButton = new Button(VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        editButton.addClickListener(e -> openFormDialog(dosen));
        editButton.getStyle().set("color", "#059669");

        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        deleteButton.addClickListener(e -> deleteDosen(dosen));
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
                .set("border-radius", "8px");

        FormLayout formLayout = createFormLayout();

        Button saveButton = new Button("Simpan", e -> saveDosen());
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
        nip = new TextField("NIP");
        email = new EmailField("Email");
        telepon = new TextField("Telepon");
        alamat = new TextField("Alamat");
        tanggalLahir = new DatePicker("Tanggal Lahir");
        password = new PasswordField("Password");

        jenisKelamin = new ComboBox<>("Jenis Kelamin");
        jenisKelamin.setItems("L", "P");

        // Professional info fields
        jabatan = new TextField("Jabatan");
        fakultas = new TextField("Fakultas");
        bidangKeahlian = new TextField("Bidang Keahlian");

        statusDosen = new ComboBox<>("Status Dosen");
        statusDosen.setItems("AKTIF", "CUTI", "PENSIUN", "NON_AKTIF");
        statusDosen.setValue("AKTIF");

        // Setup binder
        binder.forField(nama).asRequired("Nama wajib diisi").bind(Dosen::getNama, Dosen::setNama);
        binder.forField(nip).asRequired("NIP wajib diisi").bind(Dosen::getNip, Dosen::setNip);
        binder.forField(email).asRequired("Email wajib diisi")
                .withValidator(new EmailValidator("Format email tidak valid"))
                .bind(Dosen::getEmail, Dosen::setEmail);
        binder.forField(telepon).bind(Dosen::getTelepon, Dosen::setTelepon);
        binder.forField(alamat).bind(Dosen::getAlamat, Dosen::setAlamat);
        binder.forField(tanggalLahir).bind(Dosen::getTanggalLahir, Dosen::setTanggalLahir);
        binder.forField(jenisKelamin).bind(Dosen::getJenisKelamin, Dosen::setJenisKelamin);
        binder.forField(jabatan).asRequired("Jabatan wajib diisi").bind(Dosen::getJabatan, Dosen::setJabatan);
        binder.forField(fakultas).asRequired("Fakultas wajib diisi").bind(Dosen::getFakultas, Dosen::setFakultas);
        binder.forField(bidangKeahlian).bind(Dosen::getBidangKeahlian, Dosen::setBidangKeahlian);
        binder.forField(statusDosen).bind(Dosen::getStatusDosen, Dosen::setStatusDosen);
        binder.forField(password).asRequired("Password wajib diisi").bind(Dosen::getPassword, Dosen::setPassword);

        // Set colspan for full-width fields
        formLayout.setColspan(alamat, 2);
        formLayout.setColspan(bidangKeahlian, 2);
        formLayout.setColspan(password, 2);

        formLayout.add(nama, nip, email, telepon, alamat, tanggalLahir, jenisKelamin,
                jabatan, fakultas, bidangKeahlian, statusDosen, password);

        return formLayout;
    }

    private void openFormDialog(Dosen dosen) {
        currentDosen = dosen;
        binder.setBean(dosen);

        if (dosen.getId() != null) {
            formDialog.setHeaderTitle("Edit Dosen");
            password.setVisible(false);
        } else {
            formDialog.setHeaderTitle("Tambah Dosen");
            password.setVisible(true);
        }

        formDialog.open();
    }

    private void saveDosen() {
        try {
            if (binder.validate().isOk()) {
                currentDosen.setRole(Role.DOSEN);
                personService.saveDosen(currentDosen);
                loadData();
                formDialog.close();
                Notification.show("Dosen berhasil disimpan", 3000, Notification.Position.TOP_CENTER);
            }
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void deleteDosen(Dosen dosen) {
        try {
            personService.deleteById(dosen.getId());
            loadData();
            Notification.show("Dosen berhasil dihapus", 3000, Notification.Position.TOP_CENTER);
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void filterDosen(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            loadData();
        } else {
            List<Dosen> filtered = personService.findAllDosen().stream()
                    .filter(d -> d.getNama().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            d.getNip().toLowerCase().contains(searchTerm.toLowerCase()) ||
                            d.getFakultas().toLowerCase().contains(searchTerm.toLowerCase()))
                    .toList();
            grid.setItems(filtered);
        }
    }

    private void loadData() {
        grid.setItems(personService.findAllDosen());
    }
}