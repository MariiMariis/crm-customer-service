CREATE SEQUENCE revinfo_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE revinfo (
    rev      INTEGER      NOT NULL,
    revtstmp BIGINT       NOT NULL,
    actor    VARCHAR(120) NOT NULL,
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

CREATE INDEX idx_revinfo_timestamp ON revinfo (revtstmp);

CREATE SEQUENCE products_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE product_sku_seq START WITH 1 INCREMENT BY 1 MAXVALUE 999999;

CREATE TABLE products (
    id                   BIGINT                   NOT NULL,
    sku                  VARCHAR(20)              NOT NULL,
    name                 VARCHAR(160)             NOT NULL,
    description          VARCHAR(2000),
    category             VARCHAR(20)              NOT NULL,
    subcategory          VARCHAR(30)              NOT NULL,
    billing              VARCHAR(20)              NOT NULL,
    unit                 VARCHAR(20)              NOT NULL,
    unit_price           NUMERIC(15, 2)           NOT NULL,
    unit_cost            NUMERIC(15, 2),
    max_discount_percent NUMERIC(5, 2)            NOT NULL,
    manufacturer         VARCHAR(80),
    warranty_months      INTEGER,
    active               BOOLEAN                  NOT NULL,
    archived             BOOLEAN                  NOT NULL DEFAULT FALSE,
    archived_at          TIMESTAMP WITH TIME ZONE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by           VARCHAR(120),
    updated_by           VARCHAR(120),
    version              BIGINT                   NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT ck_products_category CHECK (category IN ('SOFTWARE', 'HARDWARE', 'SERVICE')),
    CONSTRAINT ck_products_billing CHECK (
        (category = 'SOFTWARE' AND billing IN ('ONE_TIME', 'MONTHLY', 'ANNUAL'))
        OR (category = 'HARDWARE' AND billing = 'ONE_TIME')
        OR (category = 'SERVICE' AND billing IN ('ONE_TIME', 'MONTHLY'))
    ),
    CONSTRAINT ck_products_price CHECK (unit_price > 0),
    CONSTRAINT ck_products_cost CHECK (unit_cost IS NULL OR unit_cost >= 0),
    CONSTRAINT ck_products_discount CHECK (max_discount_percent BETWEEN 0 AND 100),
    CONSTRAINT ck_products_warranty CHECK (warranty_months IS NULL OR (category = 'HARDWARE' AND warranty_months >= 0))
);

CREATE INDEX idx_products_category ON products (category, subcategory);
CREATE INDEX idx_products_name ON products (lower(name));
CREATE INDEX idx_products_active_archived ON products (active, archived);

CREATE TABLE products_aud (
    id                   BIGINT   NOT NULL,
    rev                  INTEGER  NOT NULL,
    revtype              SMALLINT,
    sku                  VARCHAR(20),
    name                 VARCHAR(160),
    description          VARCHAR(2000),
    category             VARCHAR(20),
    subcategory          VARCHAR(30),
    billing              VARCHAR(20),
    unit                 VARCHAR(20),
    unit_price           NUMERIC(15, 2),
    unit_cost            NUMERIC(15, 2),
    max_discount_percent NUMERIC(5, 2),
    manufacturer         VARCHAR(80),
    warranty_months      INTEGER,
    active               BOOLEAN,
    archived             BOOLEAN,
    archived_at          TIMESTAMP WITH TIME ZONE,
    created_at           TIMESTAMP WITH TIME ZONE,
    updated_at           TIMESTAMP WITH TIME ZONE,
    created_by           VARCHAR(120),
    updated_by           VARCHAR(120),
    CONSTRAINT pk_products_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_products_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);
