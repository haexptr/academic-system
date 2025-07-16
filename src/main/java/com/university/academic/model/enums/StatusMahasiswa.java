// StatusMahasiswa.java
package com.university.academic.model.enums;

public enum StatusMahasiswa {
    AKTIF("Aktif"),
    CUTI("Cuti"),
    LULUS("Lulus"),
    DO("Drop Out"),
    MENGUNDURKAN_DIRI("Mengundurkan Diri");

    private final String displayName;

    StatusMahasiswa(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}