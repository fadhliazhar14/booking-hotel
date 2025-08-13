-- Add new columns to room_amenity table for entity relationship
ALTER TABLE room_amenity 
ADD COLUMN amenity_type_id BIGINT,
ADD COLUMN created_on DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
ADD COLUMN updated_on DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

-- Add foreign key constraint
ALTER TABLE room_amenity 
ADD CONSTRAINT fk_room_amenity_amenity_type 
FOREIGN KEY (amenity_type_id) REFERENCES amenity_type(id);

-- Migrate existing enum data to new structure (if any exists)
UPDATE room_amenity ra
JOIN amenity_type at ON UPPER(ra.amenity) = at.name
SET ra.amenity_type_id = at.id
WHERE ra.amenity_type_id IS NULL;

-- Remove old enum column after migration (commented out for safety)
-- ALTER TABLE room_amenity DROP COLUMN amenity;

-- Update room_service table structure
ALTER TABLE room_service
ADD COLUMN service_type_id BIGINT,
ADD COLUMN service_date DATE,
ADD COLUMN quantity INT DEFAULT 1,
ADD COLUMN notes TEXT,
ADD COLUMN created_on DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
ADD COLUMN updated_on DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

-- Add foreign key constraint
ALTER TABLE room_service
ADD CONSTRAINT fk_room_service_service_type
FOREIGN KEY (service_type_id) REFERENCES service_type(id);

-- Migrate existing enum data to new structure (if any exists)
UPDATE room_service rs
JOIN service_type st ON UPPER(rs.room_service_type) = st.name
SET rs.service_type_id = st.id
WHERE rs.service_type_id IS NULL;

-- Copy date field if exists
UPDATE room_service SET service_date = date WHERE service_date IS NULL AND date IS NOT NULL;

-- Remove old enum column after migration (commented out for safety)
-- ALTER TABLE room_service DROP COLUMN room_service_type;
-- ALTER TABLE room_service DROP COLUMN date;