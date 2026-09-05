CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- IAM - Initial Database Schema
-- Flyway Migration V1
-- ============================================================

-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    
    employee_id VARCHAR(50) UNIQUE,
    department VARCHAR(100),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    last_active TIMESTAMP,
    application_count INTEGER NOT NULL DEFAULT 0,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT chk_users_status 
        CHECK(status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
); 

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_employee_id ON users(employee_id);
CREATE INDEX idx_users_department ON users(department);
CREATE INDEX idx_users_status ON users(status);


-- ============================================================
-- ROLES
-- ============================================================

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);


-- ============================================================
-- PERMISSIONS
-- ============================================================

CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);


-- ============================================================
-- ROLE ↔ PERMISSION
-- ============================================================

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,

    PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
        REFERENCES permissions(id)
        ON DELETE CASCADE
);


-- ============================================================
-- USER ↔ ROLE
-- ============================================================

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE
);


-- ============================================================
-- APPLICATIONS
-- ============================================================

CREATE TABLE applications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,

    owner VARCHAR(255),
    department VARCHAR(100),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT chk_applications_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_applications_status
    ON applications(status);


-- ============================================================
-- APPLICATION ACCESS
-- ============================================================

CREATE TABLE application_access (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    application_id UUID NOT NULL,
    user_id UUID NOT NULL,

    access_level VARCHAR(50) NOT NULL,

    granted_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_application_access_application
        FOREIGN KEY (application_id)
        REFERENCES applications(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_application_access_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_application_access_status
        CHECK (status IN ('ACTIVE', 'EXPIRED', 'REVOKED')),

    CONSTRAINT uq_application_user_access
        UNIQUE (application_id, user_id)
);

CREATE INDEX idx_application_access_user
    ON application_access(user_id);

CREATE INDEX idx_application_access_application
    ON application_access(application_id);


-- ============================================================
-- ACCESS REQUESTS
-- ============================================================

CREATE TABLE access_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    requester_id UUID NOT NULL,
    application_id UUID NOT NULL,

    access_level VARCHAR(50) NOT NULL,

    justification TEXT NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    approver_id UUID,

    requested_at TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP,

    rejection_reason TEXT,

    CONSTRAINT fk_access_requests_requester
        FOREIGN KEY (requester_id)
        REFERENCES users(id),

    CONSTRAINT fk_access_requests_application
        FOREIGN KEY (application_id)
        REFERENCES applications(id),

    CONSTRAINT fk_access_requests_approver
        FOREIGN KEY (approver_id)
        REFERENCES users(id),

    CONSTRAINT chk_access_requests_status
        CHECK (
            status IN (
                'PENDING',
                'APPROVED',
                'REJECTED',
                'REVOKED',
                'CANCELLED'
            )
        )
);

CREATE INDEX idx_access_requests_requester
    ON access_requests(requester_id);

CREATE INDEX idx_access_requests_application
    ON access_requests(application_id);

CREATE INDEX idx_access_requests_status
    ON access_requests(status);

CREATE INDEX idx_access_requests_approver
    ON access_requests(approver_id);


-- ============================================================
-- AUDIT LOGS
-- ============================================================

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    actor_id UUID,

    action VARCHAR(100) NOT NULL,

    entity_type VARCHAR(100),
    entity_id UUID,

    description TEXT,

    ip_address VARCHAR(45),

    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_audit_logs_actor
        FOREIGN KEY (actor_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_audit_logs_actor
    ON audit_logs(actor_id);

CREATE INDEX idx_audit_logs_action
    ON audit_logs(action);

CREATE INDEX idx_audit_logs_entity
    ON audit_logs(entity_type, entity_id);

CREATE INDEX idx_audit_logs_created_at
    ON audit_logs(created_at); 