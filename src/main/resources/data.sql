-- data.sql - Versi yang diperbaiki
-- Insert person dulu
INSERT IGNORE INTO person (nama, email, telepon, alamat, tanggal_lahir, jenis_kelamin, role, password)
VALUES ('Administrator', 'admin@university.ac.id', '081234567890', 'Jl. Admin No. 1', '1980-01-01', 'L', 'ADMIN', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');

INSERT IGNORE INTO person (nama, email, telepon, alamat, tanggal_lahir, jenis_kelamin, role, password)
VALUES ('Staff TU', 'staff@university.ac.id', '081234567891', 'Jl. Staff No. 1', '1985-05-15', 'P', 'STAFF', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');

-- Insert staff dengan menggunakan ID dari person yang baru dibuat
INSERT IGNORE INTO staff (id, nip, departemen, jabatan, status_kerja)
SELECT p.id, '198505152010032001', 'Tata Usaha', 'ADMIN', 'AKTIF'
FROM person p
WHERE p.email = 'staff@university.ac.id'
  AND NOT EXISTS (SELECT 1 FROM staff s WHERE s.id = p.id);