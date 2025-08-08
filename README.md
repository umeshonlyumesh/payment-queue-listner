-- 1️⃣ Create the trigger function
CREATE OR REPLACE FUNCTION moneymovement.sync_transaction_change_record()
RETURNS TRIGGER AS $$
BEGIN
    -- Upsert into target table
    INSERT INTO moneymovement.transaction_change_record (
        uid,
        archive_date,
        transaction_uid,
        transaction_category,
        lob,
        version,
        process_status
    )
    VALUES (
        NEW.uid,
        NEW.archive_date,
        NEW.uid,  -- transaction_uid same as uid from source
        NEW.state,  -- mapping example: state -> transaction_category
        NEW.lob,
        NEW.version,
        NULL       -- process_status can be NULL initially
    )
    ON CONFLICT (uid)
    DO UPDATE
    SET archive_date = EXCLUDED.archive_date,
        transaction_uid = EXCLUDED.transaction_uid,
        transaction_category = EXCLUDED.transaction_category,
        lob = EXCLUDED.lob,
        version = EXCLUDED.version,
        process_status = EXCLUDED.process_status;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2️⃣ Create the trigger
CREATE TRIGGER trg_sync_transaction_change_record
AFTER INSERT OR UPDATE
ON moneymovement.transaction
FOR EACH ROW
EXECUTE FUNCTION moneymovement.sync_transaction_change_record();
