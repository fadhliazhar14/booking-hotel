-- ======== Membatalkan Perubahan pada Tabel room_service ========

-- 1. Hapus foreign key constraint terlebih dahulu
ALTER TABLE room_service
DROP FOREIGN KEY fk_room_service_service_type;

-- 2. Hapus kolom-kolom baru yang ditambahkan pada V2
ALTER TABLE room_service
DROP COLUMN service_type_id,
DROP COLUMN service_date,
DROP COLUMN quantity,
DROP COLUMN notes,
DROP COLUMN created_on,
DROP COLUMN updated_on;


-- ======== Membatalkan Perubahan pada Tabel room_amenity ========

-- 1. Hapus foreign key constraint terlebih dahulu
ALTER TABLE room_amenity
DROP FOREIGN KEY fk_room_amenity_amenity_type;

-- 2. Hapus kolom-kolom baru yang ditambahkan pada V2
ALTER TABLE room_amenity
DROP COLUMN amenity_type_id,
DROP COLUMN created_on,
DROP COLUMN updated_on;

-- Catatan: Perintah ini tidak menambahkan kembali kolom enum lama ('amenity' dan 'room_service_type')
-- karena data aslinya mungkin sudah tidak relevan. Jika Anda perlu mengembalikannya secara penuh,
-- Anda harus menambahkan kembali kolom-kolom tersebut dan melakukan migrasi data terbalik.