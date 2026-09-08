INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'USER', 'Standard application user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'APPROVER', 'User authorized to approve access requests', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);