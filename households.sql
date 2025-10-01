DO $$
DECLARE
    i INT;
    j INT;
    new_real_estate_id BIGINT;
    num_households INT;
    total_citizens INT;
    citizens_array BIGINT[];
    citizen_idx INT;
    building_floors INT;
BEGIN
    -- Učitaj sve citizen user_id u niz
    SELECT array_agg(user_id) INTO citizens_array FROM citizens;
    total_citizens := array_length(citizens_array, 1);

    IF total_citizens IS NULL OR total_citizens = 0 THEN
        RAISE EXCEPTION 'No citizens found in database. Please run user insertion script first.';
    END IF;

    RAISE NOTICE 'Found % citizens. Starting real estate insertion...', total_citizens;

    FOR i IN 1..100000 LOOP
        -- Random broj domaćinstava
        num_households := floor(random() * 20 + 1)::INT;

        -- Spratnost
        building_floors := CASE 
            WHEN num_households <= 5 THEN floor(random() * 3 + 1)::INT
            WHEN num_households <= 10 THEN floor(random() * 5 + 3)::INT
            ELSE floor(random() * 10 + 5)::INT
        END;

        -- Insert real estate (skip ako address već postoji)
        INSERT INTO real_estates (id, address, municipality, town, floors)
        VALUES (
            DEFAULT,
            'Street ' || i || ', Building ' || (i % 100),
            CASE (i % 10)
                WHEN 0 THEN 'Novi Sad'
                WHEN 1 THEN 'Zvezdara'
                WHEN 2 THEN 'Sombor'
                WHEN 3 THEN 'Golubac'
                WHEN 4 THEN 'Topola'
                WHEN 5 THEN 'Kraljevo'
                WHEN 6 THEN 'Aleksandrovac'
                WHEN 7 THEN 'Leskovac'
                WHEN 8 THEN 'Ruma'
                ELSE 'Ada'
            END,
            CASE (i % 5)
                WHEN 0 THEN 'Springfield'
                WHEN 1 THEN 'Riverside'
                WHEN 2 THEN 'Greenville'
                WHEN 3 THEN 'Lakewood'
                ELSE 'Hilltown'
            END,
            building_floors
        )
        ON CONFLICT (id) DO NOTHING
        RETURNING id INTO new_real_estate_id;

        -- Ako je preskočen insert (jer već postoji), ne ubacuj households
        IF new_real_estate_id IS NULL THEN
            CONTINUE;
        END IF;

        -- Insert households
        FOR j IN 1..num_households LOOP
            citizen_idx := floor(random() * total_citizens + 1)::INT;

            INSERT INTO households (id, floor, square_footage, apartment_number, real_estate_id, user_id)
            VALUES (
                DEFAULT, 
                floor(random() * building_floors + 1)::INT,
                floor(random() * 100 + 30)::FLOAT,
                j,
                new_real_estate_id,
                citizens_array[citizen_idx]
            )
            ON CONFLICT (id) DO NOTHING;
        END LOOP;

        IF i % 1000 = 0 THEN
            RAISE NOTICE 'Inserted % real estates with households', i;
        END IF;
    END LOOP;

    RAISE NOTICE 'Completed! Inserted up to 100000 real estates with households.';
END $$;