-- Insert 10 admins
DO $$
DECLARE
    i INT;
BEGIN
    FOR i IN 1..10 LOOP
        INSERT INTO users (username, password, role, user_photo, secret, password_changed, is_active, activation_token)
        VALUES (
            'admin' || i || '@example.com',
            '$2a$10$2LEWWdYiQDsLoQUeA9O7Yu9LmtSQcw61L41FKe1YkxEZMa5hNPrZe', -- '12345678'
            'ADMIN',
            NULL,
            NULL,
            TRUE,
            TRUE,
            NULL
        )
        ON CONFLICT (username) DO NOTHING;
    END LOOP;
END $$;

-- Insert 200 employees
DO $$
DECLARE
    i INT;
    new_user_id BIGINT;
BEGIN
    FOR i IN 1..200 LOOP
        INSERT INTO users (username, password, role, user_photo, secret, password_changed, is_active, activation_token)
        VALUES (
            'employee' || i || '@example.com',
            '$2a$10$2LEWWdYiQDsLoQUeA9O7Yu9LmtSQcw61L41FKe1YkxEZMa5hNPrZe',
            'EMPLOYEE',
            NULL,
            NULL,
            TRUE,
            TRUE,
            NULL
        )
        ON CONFLICT (username) DO NOTHING
        RETURNING id INTO new_user_id;

        -- Ako user nije ubačen (jer već postoji), RETURNING ne vraća ništa → new_user_id = NULL
        IF new_user_id IS NOT NULL THEN
            INSERT INTO employees (user_id, name, surname, suspended, username)
            VALUES (
                new_user_id,
                'EmpName' || i,
                'EmpSurname' || i,
                FALSE,
                'employee' || i
            )
            ON CONFLICT DO NOTHING;
        END IF;
    END LOOP;
END $$;

-- Insert 1,000,000 citizens
DO $$
DECLARE
    i INT;
    new_user_id BIGINT;
BEGIN
    FOR i IN 1..1000000 LOOP
        INSERT INTO users (username, password, role, user_photo, secret, password_changed, is_active, activation_token)
        VALUES (
            'citizen' || i || '@example.com',
            '$2a$10$2LEWWdYiQDsLoQUeA9O7Yu9LmtSQcw61L41FKe1YkxEZMa5hNPrZe',
            'CITIZEN',
            NULL,
            NULL,
            TRUE,
            TRUE,
            NULL
        )
        ON CONFLICT (username) DO NOTHING
        RETURNING id INTO new_user_id;

        IF new_user_id IS NOT NULL THEN
            INSERT INTO citizens (user_id, username)
            VALUES (
                new_user_id,
                'citizen' || i
            )
            ON CONFLICT DO NOTHING;
        END IF;
    END LOOP;
END $$;
