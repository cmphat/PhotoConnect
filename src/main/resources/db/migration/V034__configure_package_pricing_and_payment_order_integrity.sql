-- Per-package pricing configuration. Existing packages keep the documented V6 defaults.
ALTER TABLE service_packages ADD
    night_surcharge_rate DECIMAL(5,4) NOT NULL
        CONSTRAINT DF_service_packages_night_surcharge_rate DEFAULT (0.2000),
    included_travel_km INT NOT NULL
        CONSTRAINT DF_service_packages_included_travel_km DEFAULT (30),
    excess_travel_rate DECIMAL(18,2) NOT NULL
        CONSTRAINT DF_service_packages_excess_travel_rate DEFAULT (15000),
    additional_hour_fee DECIMAL(18,2) NOT NULL
        CONSTRAINT DF_service_packages_additional_hour_fee DEFAULT (200000),
    extra_edited_photo_fee DECIMAL(18,2) NOT NULL
        CONSTRAINT DF_service_packages_extra_edited_photo_fee DEFAULT (50000),
    assistant_fee DECIMAL(18,2) NOT NULL
        CONSTRAINT DF_service_packages_assistant_fee DEFAULT (500000);
GO

ALTER TABLE service_packages ADD CONSTRAINT CK_service_packages_pricing_settings CHECK (
    night_surcharge_rate >= 0 AND night_surcharge_rate <= 5 AND
    included_travel_km >= 0 AND included_travel_km <= 10000 AND
    excess_travel_rate >= 0 AND additional_hour_fee >= 0 AND
    extra_edited_photo_fee >= 0 AND assistant_fee >= 0
);
GO

-- A provider order code must resolve to at most one deposit. Abort rather than mutate data
-- if historical duplicates exist; they require an explicit reconciliation decision.
IF EXISTS (
    SELECT provider_order_code
    FROM deposits
    WHERE provider_order_code IS NOT NULL
    GROUP BY provider_order_code
    HAVING COUNT(*) > 1
)
    THROW 51034, 'Duplicate provider_order_code values must be reconciled before V034.', 1;
GO

CREATE UNIQUE INDEX UQ_deposits_provider_order_code
    ON deposits(provider_order_code)
    WHERE provider_order_code IS NOT NULL;
