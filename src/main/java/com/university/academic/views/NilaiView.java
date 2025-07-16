package com.university.academic.views;

import com.university.academic.model.entity.Nilai;
import com.university.academic.model.entity.Mahasiswa;
import com.university.academic.model.entity.MataKuliah;
import com.university.academic.model.entity.Dosen;
import com.university.academic.service.AcademicService;
import com.university.academic.service.PersonService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Route(value = "nilai", layout = MainLayout.class)
@PageTitle("Nilai | Sistem Informasi Akademik")
@RolesAllowed({"ADMIN", "DOSEN", "MAHASISWA"})
public class NilaiView extends VerticalLayout {

    private final AcademicService academicService;
    private final PersonService personService;
    private final AuthenticationContext authenticationContext;
    private final Grid<Nilai> grid;
    private final Binder<Nilai> binder;

    // Form fields untuk admin/dosen
    private ComboBox<Mahasiswa> mahasiswaCombo;
    private ComboBox<MataKuliah> mataKuliahCombo;
    private ComboBox<Dosen> dosenCombo;
    private NumberField nilaiAngka;
    private TextField nilaiHuruf;
    private IntegerField semester;
    private TextField tahunAkademik;

    private Dialog formDialog;
    private Nilai currentNilai;

    public NilaiView(AcademicService academicService, PersonService personService,
                     AuthenticationContext authenticationContext) {
        this.academicService = academicService;
        this.personService = personService;
        this.authenticationContext = authenticationContext;
        this.grid = new Grid<>(Nilai.class, false);
        this.binder = new Binder<>(Nilai.class);

        setSizeFull();

        String userRole = getCurrentUserRole();
        createRoleBasedView(userRole);
    }

    private void createRoleBasedView(String userRole) {
        switch (userRole) {
            case "ROLE_MAHASISWA" -> createMahasiswaView();
            case "ROLE_DOSEN" -> createDosenView();
            case "ROLE_ADMIN" -> createAdminView();
            default -> add(new Paragraph("Akses tidak diizinkan"));
        }
    }

    // View untuk MAHASISWA - Transkrip nilai mereka (read-only)
    private void createMahasiswaView() {
        add(new H2("📋 Transkrip Nilai"));

        String currentEmail = getCurrentUserEmail();
        Optional<Mahasiswa> currentMahasiswa = personService.findByEmail(currentEmail)
                .filter(p -> p instanceof Mahasiswa)
                .map(p -> (Mahasiswa) p);

        if (currentMahasiswa.isEmpty()) {
            add(new Paragraph("Data mahasiswa tidak ditemukan."));
            return;
        }

        Mahasiswa mahasiswa = currentMahasiswa.get();

        // Info mahasiswa
        add(new Paragraph("📚 NIM: " + mahasiswa.getNim()));
        add(new Paragraph("🎓 Jurusan: " + mahasiswa.getJurusan()));
        add(new Paragraph("📈 IPK: " + (mahasiswa.getIpk() != null ? mahasiswa.getIpk() : "Belum ada")));
        add(new Paragraph("📅 Semester: " + mahasiswa.getSemester()));

        // Filter semester
        ComboBox<Integer> filterSemester = new ComboBox<>("Filter Semester");
        filterSemester.setItems(1, 2, 3, 4, 5, 6, 7, 8);
        filterSemester.addValueChangeListener(e -> filterMahasiswaNilaiBySemester(mahasiswa, e.getValue()));

        add(filterSemester);

        createMahasiswaGrid();
        loadMahasiswaNilai(mahasiswa);

        add(new Paragraph("💡 Transkrip ini menampilkan semua nilai yang telah Anda peroleh."));
    }


    // View untuk DOSEN - Input nilai mahasiswa pada mata kuliah yang diajar
    private void createDosenView() {
        add(new H2("✏️ Input Nilai Mahasiswa"));

        String currentEmail = getCurrentUserEmail();
        Optional<Dosen> currentDosen = personService.findByEmail(currentEmail)
                .filter(p -> p instanceof Dosen)
                .map(p -> (Dosen) p);

        if (currentDosen.isEmpty()) {
            add(new Paragraph("Data dosen tidak ditemukan."));
            return;
        }

        Dosen dosen = currentDosen.get();
        add(new Paragraph("👨‍🏫 Dosen: " + dosen.getNama()));
        add(new Paragraph("🏫 Fakultas: " + dosen.getFakultas()));

        // Filter untuk dosen - hanya mata kuliah yang diajar dosen ini
        ComboBox<MataKuliah> filterMataKuliah = new ComboBox<>("Filter Mata Kuliah");

        // Ambil mata kuliah yang diajar dosen ini dari jadwal
        List<MataKuliah> mataKuliahDiajar = academicService.findAllJadwal().stream()
                .filter(j -> j.getDosen().getId().equals(dosen.getId()))
                .map(j -> j.getMataKuliah())
                .distinct()
                .toList();

        filterMataKuliah.setItems(mataKuliahDiajar);
        filterMataKuliah.setItemLabelGenerator(MataKuliah::toString);
        filterMataKuliah.addValueChangeListener(e -> filterDosenNilaiByMataKuliah(dosen, e.getValue()));

        // Tombol input nilai (terbatas untuk mata kuliah yang diajar)
        Button addButton = new Button("Input Nilai", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openDosenFormDialog(new Nilai(), dosen));

        // Perbaikan layout toolbar - tambahkan alignment dan justify content
        HorizontalLayout toolbar = new HorizontalLayout(filterMataKuliah, addButton);
        toolbar.setAlignItems(Alignment.END); // Sejajarkan ke bawah (baseline)
        toolbar.setJustifyContentMode(JustifyContentMode.START); // Posisi dari kiri
        toolbar.setSpacing(true); // Berikan spacing antar komponen
        toolbar.setPadding(false); // Hilangkan padding default

        add(toolbar);

        createDosenGrid();
        createFormDialog();
        loadDosenNilai(dosen);

        add(new Paragraph("📝 Anda dapat menginput dan mengedit nilai mahasiswa pada mata kuliah yang Anda ajar."));
    }

    // View untuk ADMIN - Full management
    private void createAdminView() {
        createAdminToolbar();
        createAdminGrid();
        createFormDialog();
        loadData();

        add(new Paragraph("🔧 Sebagai Admin, Anda dapat mengelola semua data nilai mahasiswa."));
    }

    private void createMahasiswaGrid() {
        grid.removeAllColumns();
        grid.addColumn(nilai -> nilai.getMataKuliah().getKodeMK()).setHeader("Kode MK");
        grid.addColumn(nilai -> nilai.getMataKuliah().getNamaMK()).setHeader("Mata Kuliah");
        grid.addColumn(nilai -> nilai.getMataKuliah().getSks() + " SKS").setHeader("SKS");
        grid.addColumn(nilai -> nilai.getNilaiAngka() != null ? nilai.getNilaiAngka().toString() : "-")
                .setHeader("Nilai Angka");
        grid.addColumn(nilai -> nilai.getNilaiHuruf() != null ? nilai.getNilaiHuruf() : "-").setHeader("Nilai Huruf");
        grid.addColumn(nilai -> nilai.getSemester().toString()).setHeader("Semester");
        grid.addColumn(nilai -> nilai.getDosen().getNama()).setHeader("Dosen");

        add(grid);
    }

    private void createDosenGrid() {
        grid.removeAllColumns();
        grid.addColumn(nilai -> nilai.getMahasiswa().getNim()).setHeader("NIM");
        grid.addColumn(nilai -> nilai.getMahasiswa().getNama()).setHeader("Nama Mahasiswa");
        grid.addColumn(nilai -> nilai.getMataKuliah().getKodeMK()).setHeader("Kode MK");
        grid.addColumn(nilai -> nilai.getMataKuliah().getNamaMK()).setHeader("Mata Kuliah");
        grid.addColumn(nilai -> nilai.getNilaiAngka() != null ? nilai.getNilaiAngka().toString() : "-")
                .setHeader("Nilai Angka");
        grid.addColumn(nilai -> nilai.getNilaiHuruf() != null ? nilai.getNilaiHuruf() : "-").setHeader("Nilai Huruf");
        grid.addColumn(nilai -> nilai.getSemester().toString()).setHeader("Semester");

        // Action buttons untuk dosen (edit nilai yang sudah diinput)
        grid.addComponentColumn(this::createDosenActionButtons).setHeader("Aksi").setWidth("150px");

        add(grid);
    }

    private void createAdminGrid() {
        grid.removeAllColumns();
        grid.addColumn(nilai -> nilai.getMahasiswa().getNim()).setHeader("NIM").setSortable(true);
        grid.addColumn(nilai -> nilai.getMahasiswa().getNama()).setHeader("Nama Mahasiswa").setSortable(true);
        grid.addColumn(nilai -> nilai.getMataKuliah().getKodeMK()).setHeader("Kode MK").setSortable(true);
        grid.addColumn(nilai -> nilai.getMataKuliah().getNamaMK()).setHeader("Mata Kuliah").setSortable(true);
        grid.addColumn(nilai -> nilai.getNilaiAngka() != null ? nilai.getNilaiAngka().toString() : "-")
                .setHeader("Nilai Angka").setSortable(true);
        grid.addColumn(nilai -> nilai.getNilaiHuruf() != null ? nilai.getNilaiHuruf() : "-").setHeader("Nilai Huruf").setSortable(true);
        grid.addColumn(nilai -> nilai.getSemester().toString()).setHeader("Semester").setSortable(true);
        grid.addColumn(nilai -> nilai.getDosen().getNama()).setHeader("Dosen").setSortable(true);

        grid.addComponentColumn(this::createActionButtons).setHeader("Aksi").setWidth("200px");
        add(grid);
    }

    private void createAdminToolbar() {
        Button addButton = new Button("Input Nilai", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openFormDialog(new Nilai()));

        ComboBox<Mahasiswa> filterMahasiswa = new ComboBox<>("Filter Mahasiswa");
        filterMahasiswa.setItems(personService.findAllMahasiswa());
        filterMahasiswa.setItemLabelGenerator(Mahasiswa::getDisplayInfo);
        filterMahasiswa.addValueChangeListener(e -> filterByMahasiswa(e.getValue()));

        HorizontalLayout toolbar = new HorizontalLayout(new H2("🏆 Manajemen Nilai"), addButton, filterMahasiswa);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(Alignment.CENTER);

        add(toolbar);
    }

    private HorizontalLayout createDosenActionButtons(Nilai nilai) {
        // Dosen hanya bisa edit nilai yang dia input
        String currentEmail = getCurrentUserEmail();
        boolean canEdit = nilai.getDosen().getEmail().equals(currentEmail);

        Button editButton = new Button(VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        editButton.setEnabled(canEdit);
        editButton.addClickListener(e -> {
            if (canEdit) {
                openDosenEditDialog(nilai);
            }
        });

        return new HorizontalLayout(editButton);
    }

    private HorizontalLayout createActionButtons(Nilai nilai) {
        Button editButton = new Button(VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        editButton.addClickListener(e -> openFormDialog(nilai));

        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        deleteButton.addClickListener(e -> deleteNilai(nilai));

        return new HorizontalLayout(editButton, deleteButton);
    }

    private void loadMahasiswaNilai(Mahasiswa mahasiswa) {
        List<Nilai> nilaiMahasiswa = academicService.findNilaiByMahasiswa(mahasiswa);
        grid.setItems(nilaiMahasiswa);
    }

    private void filterMahasiswaNilaiBySemester(Mahasiswa mahasiswa, Integer semester) {
        List<Nilai> filtered = academicService.findNilaiByMahasiswa(mahasiswa).stream()
                .filter(n -> semester == null || n.getSemester().equals(semester))
                .toList();
        grid.setItems(filtered);
    }

    private void loadDosenNilai(Dosen dosen) {
        List<Nilai> nilaiDosen = academicService.findAllNilai().stream()
                .filter(n -> n.getDosen().getId().equals(dosen.getId()))
                .toList();
        grid.setItems(nilaiDosen);
    }

    private void filterDosenNilaiByMataKuliah(Dosen dosen, MataKuliah mataKuliah) {
        List<Nilai> filtered = academicService.findAllNilai().stream()
                .filter(n -> n.getDosen().getId().equals(dosen.getId()))
                .filter(n -> mataKuliah == null || n.getMataKuliah().getKodeMK().equals(mataKuliah.getKodeMK()))
                .toList();
        grid.setItems(filtered);
    }

    private void openDosenFormDialog(Nilai nilai, Dosen dosen) {
        // Pre-set dosen untuk form dosen
        nilai.setDosen(dosen);

        // Batasi pilihan mata kuliah hanya yang diajar dosen ini
        List<MataKuliah> mataKuliahDiajar = academicService.findAllJadwal().stream()
                .filter(j -> j.getDosen().getId().equals(dosen.getId()))
                .map(j -> j.getMataKuliah())
                .distinct()
                .toList();

        mataKuliahCombo.setItems(mataKuliahDiajar);

        openFormDialog(nilai);
        dosenCombo.setReadOnly(true); // Dosen tidak bisa mengubah field dosen
    }

    private void openDosenEditDialog(Nilai nilai) {
        openFormDialog(nilai);
        mahasiswaCombo.setReadOnly(true); // Tidak bisa ubah mahasiswa
        mataKuliahCombo.setReadOnly(true); // Tidak bisa ubah mata kuliah
        dosenCombo.setReadOnly(true); // Tidak bisa ubah dosen
    }

    private void createFormDialog() {
        formDialog = new Dialog();
        formDialog.setWidth("500px");
        formDialog.setHeight("600px");

        FormLayout formLayout = createFormLayout();

        Button saveButton = new Button("Simpan", e -> saveNilai());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Batal", e -> {
            resetFormReadOnly();
            formDialog.close();
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        VerticalLayout dialogContent = new VerticalLayout(formLayout, buttonLayout);
        formDialog.add(dialogContent);
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();

        mahasiswaCombo = new ComboBox<>("Mahasiswa");
        mahasiswaCombo.setItems(personService.findAllMahasiswa());
        mahasiswaCombo.setItemLabelGenerator(Mahasiswa::getDisplayInfo);

        mataKuliahCombo = new ComboBox<>("Mata Kuliah");
        mataKuliahCombo.setItems(academicService.findAllMataKuliah());
        mataKuliahCombo.setItemLabelGenerator(MataKuliah::toString);

        dosenCombo = new ComboBox<>("Dosen");
        dosenCombo.setItems(personService.findAllDosen());
        dosenCombo.setItemLabelGenerator(Dosen::getDisplayInfo);

        nilaiAngka = new NumberField("Nilai Angka");
        nilaiAngka.setMin(0);
        nilaiAngka.setMax(100);
        nilaiAngka.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                updateNilaiHuruf(e.getValue());
            }
        });

        nilaiHuruf = new TextField("Nilai Huruf");
        nilaiHuruf.setReadOnly(true);

        semester = new IntegerField("Semester");
        semester.setMin(1);
        semester.setMax(14);

        tahunAkademik = new TextField("Tahun Akademik");
        tahunAkademik.setPlaceholder("2023/2024");

        // Setup binder
        binder.forField(mahasiswaCombo).asRequired("Mahasiswa wajib dipilih")
                .bind(Nilai::getMahasiswa, Nilai::setMahasiswa);
        binder.forField(mataKuliahCombo).asRequired("Mata kuliah wajib dipilih")
                .bind(Nilai::getMataKuliah, Nilai::setMataKuliah);
        binder.forField(dosenCombo).asRequired("Dosen wajib dipilih")
                .bind(Nilai::getDosen, Nilai::setDosen);
        binder.forField(nilaiAngka).asRequired("Nilai angka wajib diisi")
                .withConverter(
                        value -> value != null ? BigDecimal.valueOf(value) : null,
                        value -> value != null ? value.doubleValue() : null)
                .bind(Nilai::getNilaiAngka, Nilai::setNilaiAngka);
        binder.forField(nilaiHuruf).bind(Nilai::getNilaiHuruf, Nilai::setNilaiHuruf);
        binder.forField(semester).asRequired("Semester wajib diisi")
                .bind(Nilai::getSemester, Nilai::setSemester);
        binder.forField(tahunAkademik).bind(Nilai::getTahunAkademik, Nilai::setTahunAkademik);

        formLayout.add(mahasiswaCombo, mataKuliahCombo, dosenCombo, nilaiAngka,
                nilaiHuruf, semester, tahunAkademik);

        return formLayout;
    }

    private void resetFormReadOnly() {
        mahasiswaCombo.setReadOnly(false);
        mataKuliahCombo.setReadOnly(false);
        dosenCombo.setReadOnly(false);
    }

    private void updateNilaiHuruf(Double nilai) {
        if (nilai >= 85) {
            nilaiHuruf.setValue("A");
        } else if (nilai >= 80) {
            nilaiHuruf.setValue("A-");
        } else if (nilai >= 75) {
            nilaiHuruf.setValue("B+");
        } else if (nilai >= 70) {
            nilaiHuruf.setValue("B");
        } else if (nilai >= 65) {
            nilaiHuruf.setValue("B-");
        } else if (nilai >= 60) {
            nilaiHuruf.setValue("C+");
        } else if (nilai >= 55) {
            nilaiHuruf.setValue("C");
        } else if (nilai >= 50) {
            nilaiHuruf.setValue("C-");
        } else if (nilai >= 45) {
            nilaiHuruf.setValue("D");
        } else {
            nilaiHuruf.setValue("E");
        }
    }

    private void openFormDialog(Nilai nilai) {
        currentNilai = nilai;
        binder.setBean(nilai);

        if (nilai.getId() != null) {
            formDialog.setHeaderTitle("Edit Nilai");
        } else {
            formDialog.setHeaderTitle("Input Nilai");
        }

        formDialog.open();
    }

    private void saveNilai() {
        try {
            if (binder.validate().isOk()) {
                // Validasi untuk dosen
                String userRole = getCurrentUserRole();
                if ("ROLE_DOSEN".equals(userRole)) {
                    String currentEmail = getCurrentUserEmail();
                    if (!currentNilai.getDosen().getEmail().equals(currentEmail)) {
                        Notification.show("Anda hanya dapat menginput nilai untuk mata kuliah yang Anda ajar",
                                3000, Notification.Position.TOP_CENTER);
                        return;
                    }
                }

                // Panggil konversiNilai() sebelum save
                currentNilai.konversiNilai();

                academicService.saveNilai(currentNilai);

                // Reload data berdasarkan role
                String userRole2 = getCurrentUserRole();
                if ("ROLE_MAHASISWA".equals(userRole2)) {
                    // Reload untuk mahasiswa
                    String currentEmail = getCurrentUserEmail();
                    personService.findByEmail(currentEmail)
                            .filter(p -> p instanceof Mahasiswa)
                            .map(p -> (Mahasiswa) p)
                            .ifPresent(this::loadMahasiswaNilai);
                } else if ("ROLE_DOSEN".equals(userRole2)) {
                    // Reload untuk dosen
                    String currentEmail = getCurrentUserEmail();
                    personService.findByEmail(currentEmail)
                            .filter(p -> p instanceof Dosen)
                            .map(p -> (Dosen) p)
                            .ifPresent(this::loadDosenNilai);
                } else {
                    loadData();
                }

                resetFormReadOnly();
                formDialog.close();
                Notification.show("Nilai berhasil disimpan", 3000, Notification.Position.TOP_CENTER);
            }
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void deleteNilai(Nilai nilai) {
        try {
            academicService.deleteNilaiById(nilai.getId()); // Perlu ditambahkan method ini di AcademicService
            loadData();
            Notification.show("Nilai berhasil dihapus", 3000, Notification.Position.TOP_CENTER);
        } catch (Exception e) {
            Notification.show("Error: " + e.getMessage(), 3000, Notification.Position.TOP_CENTER);
        }
    }

    private void filterByMahasiswa(Mahasiswa mahasiswa) {
        if (mahasiswa == null) {
            loadData();
        } else {
            grid.setItems(academicService.findNilaiByMahasiswa(mahasiswa));
        }
    }

    private void loadData() {
        grid.setItems(academicService.findAllNilai());
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