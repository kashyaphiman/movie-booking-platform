-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Create movies table
CREATE TABLE IF NOT EXISTS movies (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    language VARCHAR(50),
    genre VARCHAR(100),
    duration_minutes INTEGER,
    release_date DATE,
    description TEXT,
    poster_url VARCHAR(500),
    trailer_url VARCHAR(500),
    cast TEXT,
    director VARCHAR(255),
    rating DECIMAL(3,2),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create theatres table
CREATE TABLE IF NOT EXISTS theatres (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address TEXT,
    pin_code VARCHAR(10),
    contact_number VARCHAR(20),
    email VARCHAR(255),
    total_screens INTEGER,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create shows table
CREATE TABLE IF NOT EXISTS shows (
    id VARCHAR(36) PRIMARY KEY,
    movie_id VARCHAR(36) NOT NULL REFERENCES movies(id),
    theatre_id VARCHAR(36) NOT NULL REFERENCES theatres(id),
    show_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    base_price DECIMAL(10,2) NOT NULL,
    show_type VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    total_seats INTEGER,
    available_seats INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create seats table
CREATE TABLE IF NOT EXISTS seats (
    id VARCHAR(36) PRIMARY KEY,
    show_id VARCHAR(36) NOT NULL REFERENCES shows(id),
    seat_number VARCHAR(10) NOT NULL,
    row_code VARCHAR(2),
    seat_column INTEGER,
    seat_type VARCHAR(20),
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    blocked_by VARCHAR(36),
    blocked_until TIMESTAMP,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(show_id, seat_number)
);

-- Create bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id VARCHAR(36) PRIMARY KEY,
    booking_reference VARCHAR(50) UNIQUE NOT NULL,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    show_id VARCHAR(36) NOT NULL REFERENCES shows(id),
    number_of_seats INTEGER NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    final_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    seats_booked TEXT,
    booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_deadline TIMESTAMP,
    confirmed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    payment_id VARCHAR(255)
);

-- Create indexes for performance
CREATE INDEX idx_shows_movie_theatre ON shows(movie_id, theatre_id);
CREATE INDEX idx_shows_show_time ON shows(show_time);
CREATE INDEX idx_shows_theatre_time ON shows(theatre_id, show_time);
CREATE INDEX idx_seats_show_status ON seats(show_id, status);
CREATE INDEX idx_bookings_user_status ON bookings(user_id, status);
CREATE INDEX idx_bookings_reference ON bookings(booking_reference);
CREATE INDEX idx_bookings_show_id ON bookings(show_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_movies_title ON movies(title);
CREATE INDEX idx_theatres_city ON theatres(city);

-- Insert sample data
INSERT INTO users (id, email, password, full_name, role, email_verified, active)
VALUES ('user1', 'customer@example.com', '$2a$10$YourHashedPasswordHere', 'John Doe', 'ROLE_CUSTOMER', true, true);

INSERT INTO movies (id, title, language, genre, duration_minutes, release_date, rating, active)
VALUES
('movie1', 'Inception', 'English', 'Sci-Fi', 148, '2010-07-16', 8.8, true),
('movie2', 'The Dark Knight', 'English', 'Action', 152, '2008-07-18', 9.0, true),
('movie3', 'Interstellar', 'English', 'Sci-Fi', 169, '2014-11-07', 8.6, true);

INSERT INTO theatres (id, name, city, address, total_screens, status)
VALUES
('theatre1', 'PVR Cinemas', 'Mumbai', 'Andheri West', 5, 'ACTIVE'),
('theatre2', 'INOX', 'Mumbai', 'Bandra Kurla Complex', 4, 'ACTIVE');