-- Create amenity_type table
CREATE TABLE amenity_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_on DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_on DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

-- Create service_type table
CREATE TABLE service_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    default_price DECIMAL(19,2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT TRUE,
    created_on DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_on DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

-- Insert default amenity types (from existing enum values)
INSERT INTO amenity_type (name, description, is_active) VALUES
('TELEVISION', 'Television in the room', TRUE),
('INTERNET_ACCESS', 'High-speed internet access', TRUE),
('HAIR_DRYER', 'Hair dryer available in bathroom', TRUE),
('PREMIUM_TOWELS', 'Premium quality towels', TRUE),
('CRIB', 'Baby crib available upon request', TRUE),
('SAFE', 'In-room safe for valuables', TRUE);

-- Insert default service types (from existing enum values)
INSERT INTO service_type (name, description, default_price, is_active) VALUES
('BIR', 'Beer service', 50.00, TRUE),
('WHISKY', 'Whisky service', 150.00, TRUE);