-- BLOTTER MANAGEMENT SYSTEM DATABASE SCHEMA
-- Run this in your PostgreSQL database (Neon.tech or Render.com)

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. USERS TABLE
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'User',
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    profile_picture VARCHAR(255),
    has_selected_profile_picture BOOLEAN DEFAULT FALSE,
    must_change_password BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. OFFICERS TABLE
CREATE TABLE officers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    badge_number VARCHAR(50),
    rank VARCHAR(50),
    specialization VARCHAR(100),
    assigned_cases_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. BLOTTER REPORTS TABLE
CREATE TABLE blotter_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    blotter_number VARCHAR(50) UNIQUE,
    incident_type VARCHAR(100) NOT NULL,
    incident_date TIMESTAMP NOT NULL,
    incident_time VARCHAR(10) NOT NULL,
    location TEXT NOT NULL,
    description TEXT NOT NULL,
    complainant_name VARCHAR(200) NOT NULL,
    complainant_contact VARCHAR(20),
    complainant_address TEXT,
    status VARCHAR(50) DEFAULT 'Pending',
    priority VARCHAR(20) DEFAULT 'Normal',
    assigned_officer_id UUID REFERENCES officers(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. RESPONDENTS TABLE
CREATE TABLE respondents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID NOT NULL REFERENCES blotter_reports(id) ON DELETE CASCADE,
    person_id UUID,
    name VARCHAR(200) NOT NULL,
    age INTEGER,
    gender VARCHAR(20),
    contact VARCHAR(20),
    address TEXT,
    cooperation_status VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. SUSPECTS TABLE
CREATE TABLE suspects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID NOT NULL REFERENCES blotter_reports(id) ON DELETE CASCADE,
    person_id UUID,
    name VARCHAR(200) NOT NULL,
    alias VARCHAR(100),
    age INTEGER,
    gender VARCHAR(20),
    contact VARCHAR(20),
    address TEXT,
    physical_description TEXT,
    evidence_found TEXT,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. WITNESSES TABLE
CREATE TABLE witnesses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID NOT NULL REFERENCES blotter_reports(id) ON DELETE CASCADE,
    person_id UUID,
    name VARCHAR(200) NOT NULL,
    age INTEGER,
    gender VARCHAR(20),
    contact VARCHAR(20),
    address TEXT,
    statement TEXT,
    reliability VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. EVIDENCE TABLE
CREATE TABLE evidence (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID NOT NULL REFERENCES blotter_reports(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    file_path VARCHAR(500),
    collected_by VARCHAR(100),
    collected_date TIMESTAMP,
    storage_location VARCHAR(200),
    chain_of_custody TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. HEARINGS TABLE
CREATE TABLE hearings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID NOT NULL REFERENCES blotter_reports(id) ON DELETE CASCADE,
    hearing_date DATE NOT NULL,
    hearing_time VARCHAR(10) NOT NULL,
    location VARCHAR(200) NOT NULL,
    purpose TEXT,
    attendees TEXT,
    outcome TEXT,
    notes TEXT,
    status VARCHAR(50) DEFAULT 'Scheduled',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. RESOLUTIONS TABLE
CREATE TABLE resolutions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID NOT NULL REFERENCES blotter_reports(id) ON DELETE CASCADE,
    resolution_type VARCHAR(100) NOT NULL,
    details TEXT NOT NULL,
    resolved_by VARCHAR(100),
    resolution_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    follow_up_required BOOLEAN DEFAULT FALSE,
    follow_up_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. RESPONDENT STATEMENTS TABLE
CREATE TABLE respondent_statements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    respondent_id UUID NOT NULL REFERENCES respondents(id) ON DELETE CASCADE,
    statement TEXT NOT NULL,
    recorded_by VARCHAR(100),
    recorded_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 11. SMS NOTIFICATIONS TABLE
CREATE TABLE sms_notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID REFERENCES blotter_reports(id),
    respondent_id UUID REFERENCES respondents(id),
    recipient_name VARCHAR(200) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'Pending',
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 12. ACTIVITY LOGS TABLE
CREATE TABLE activity_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id UUID,
    details TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 13. NOTIFICATIONS TABLE
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    related_entity_type VARCHAR(50),
    related_entity_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 14. PERSON HISTORY TABLE
CREATE TABLE person_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    person_id UUID NOT NULL,
    report_id UUID NOT NULL REFERENCES blotter_reports(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    incident_type VARCHAR(100),
    incident_date TIMESTAMP,
    outcome VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 15. STATUS HISTORY TABLE
CREATE TABLE status_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    report_id UUID NOT NULL REFERENCES blotter_reports(id) ON DELETE CASCADE,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by UUID REFERENCES users(id),
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CREATE INDEXES FOR BETTER PERFORMANCE
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_officers_user_id ON officers(user_id);
CREATE INDEX idx_reports_user_id ON blotter_reports(user_id);
CREATE INDEX idx_reports_status ON blotter_reports(status);
CREATE INDEX idx_reports_assigned_officer ON blotter_reports(assigned_officer_id);
CREATE INDEX idx_respondents_report_id ON respondents(report_id);
CREATE INDEX idx_suspects_report_id ON suspects(report_id);
CREATE INDEX idx_witnesses_report_id ON witnesses(report_id);
CREATE INDEX idx_evidence_report_id ON evidence(report_id);
CREATE INDEX idx_hearings_report_id ON hearings(report_id);
CREATE INDEX idx_resolutions_report_id ON resolutions(report_id);

-- INSERT SAMPLE DATA (Optional - for testing)

-- Sample Admin User
INSERT INTO users (username, password, first_name, last_name, role, status)
VALUES ('admin', 'admin123', 'Admin', 'User', 'Admin', 'active');

-- Sample Officer User
INSERT INTO users (username, password, first_name, last_name, role, status, must_change_password)
VALUES ('officer1', 'officer123', 'Juan', 'Dela Cruz', 'Officer', 'active', FALSE);

-- Sample Clerk User
INSERT INTO users (username, password, first_name, last_name, role, status)
VALUES ('clerk1', 'clerk123', 'Maria', 'Santos', 'User', 'active');

-- Link Officer to Officers table
INSERT INTO officers (user_id, badge_number, rank)
SELECT id, 'BADGE-001', 'Police Officer I'
FROM users WHERE username = 'officer1';

-- Sample Report
INSERT INTO blotter_reports (
    user_id,
    blotter_number,
    incident_type,
    incident_date,
    incident_time,
    location,
    description,
    complainant_name,
    complainant_contact,
    status
)
SELECT 
    id,
    'BLT-' || EXTRACT(EPOCH FROM NOW())::BIGINT,
    'Theft',
    NOW(),
    '14:30',
    'Barangay Hall, CDO City',
    'Lost wallet containing cash and IDs',
    'Pedro Garcia',
    '09171234567',
    'Pending'
FROM users WHERE username = 'clerk1';

-- Success message
SELECT 'Database setup complete! 🎉' as message;
SELECT 'Total tables created: 15' as info;
SELECT 'Sample users created: 3 (admin, officer1, clerk1)' as users;
SELECT 'You can now run your API!' as next_step;
