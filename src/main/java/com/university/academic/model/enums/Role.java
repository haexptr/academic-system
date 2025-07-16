package com.university.academic.model.enums;

public enum Role {
    MAHASISWA("Mahasiswa"),
    DOSEN("Dosen"),
    STAFF("Staff"),
    ADMIN("Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}