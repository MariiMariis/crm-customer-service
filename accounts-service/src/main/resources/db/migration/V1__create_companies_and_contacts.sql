CREATE SEQUENCE revinfo_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE revinfo (
    rev      INTEGER      NOT NULL,
    revtstmp BIGINT       NOT NULL,
    actor    VARCHAR(120) NOT NULL,
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

CREATE INDEX idx_revinfo_timestamp ON revinfo (revtstmp);

CREATE TABLE sales_rep_refs (
    id        BIGINT                   NOT NULL,
    name      VARCHAR(120)             NOT NULL,
    email     VARCHAR(160),
    active    BOOLEAN                  NOT NULL,
    archived  BOOLEAN                  NOT NULL,
    synced_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_sales_rep_refs PRIMARY KEY (id)
);

CREATE SEQUENCE companies_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE companies (
    id                BIGINT                   NOT NULL,
    legal_name        VARCHAR(160)             NOT NULL,
    trade_name        VARCHAR(120),
    cnpj              VARCHAR(14)              NOT NULL,
    industry          VARCHAR(40)              NOT NULL,
    company_size      VARCHAR(20)              NOT NULL,
    employees         INTEGER,
    annual_revenue    NUMERIC(17, 2),
    website           VARCHAR(200),
    phone             VARCHAR(20),
    city              VARCHAR(100),
    state             VARCHAR(2),
    relationship_type VARCHAR(20)              NOT NULL,
    owner_id          BIGINT,
    notes             VARCHAR(2000),
    archived          BOOLEAN                  NOT NULL DEFAULT FALSE,
    archived_at       TIMESTAMP WITH TIME ZONE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by        VARCHAR(120),
    updated_by        VARCHAR(120),
    version           BIGINT                   NOT NULL,
    CONSTRAINT pk_companies PRIMARY KEY (id),
    CONSTRAINT uk_companies_cnpj UNIQUE (cnpj),
    CONSTRAINT ck_companies_size CHECK (company_size IN ('MICRO', 'SMALL', 'MEDIUM', 'LARGE', 'ENTERPRISE')),
    CONSTRAINT ck_companies_type CHECK (relationship_type IN ('PROSPECT', 'CUSTOMER', 'PARTNER', 'FORMER_CUSTOMER')),
    CONSTRAINT ck_companies_employees CHECK (employees IS NULL OR employees >= 0),
    CONSTRAINT ck_companies_revenue CHECK (annual_revenue IS NULL OR annual_revenue >= 0)
);

CREATE INDEX idx_companies_legal_name ON companies (lower(legal_name));
CREATE INDEX idx_companies_owner ON companies (owner_id);
CREATE INDEX idx_companies_industry ON companies (industry);
CREATE INDEX idx_companies_type_archived ON companies (relationship_type, archived);

CREATE TABLE companies_aud (
    id                BIGINT   NOT NULL,
    rev               INTEGER  NOT NULL,
    revtype           SMALLINT,
    legal_name        VARCHAR(160),
    trade_name        VARCHAR(120),
    cnpj              VARCHAR(14),
    industry          VARCHAR(40),
    company_size      VARCHAR(20),
    employees         INTEGER,
    annual_revenue    NUMERIC(17, 2),
    website           VARCHAR(200),
    phone             VARCHAR(20),
    city              VARCHAR(100),
    state             VARCHAR(2),
    relationship_type VARCHAR(20),
    owner_id          BIGINT,
    notes             VARCHAR(2000),
    archived          BOOLEAN,
    archived_at       TIMESTAMP WITH TIME ZONE,
    created_at        TIMESTAMP WITH TIME ZONE,
    updated_at        TIMESTAMP WITH TIME ZONE,
    created_by        VARCHAR(120),
    updated_by        VARCHAR(120),
    CONSTRAINT pk_companies_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_companies_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE SEQUENCE contacts_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE contacts (
    id            BIGINT                   NOT NULL,
    company_id    BIGINT                   NOT NULL,
    first_name    VARCHAR(80)              NOT NULL,
    last_name     VARCHAR(80)              NOT NULL,
    email         VARCHAR(160)             NOT NULL,
    phone         VARCHAR(20),
    mobile        VARCHAR(20),
    job_title     VARCHAR(100),
    department    VARCHAR(80),
    decision_role VARCHAR(30)              NOT NULL,
    is_primary    BOOLEAN                  NOT NULL,
    active        BOOLEAN                  NOT NULL,
    archived      BOOLEAN                  NOT NULL DEFAULT FALSE,
    archived_at   TIMESTAMP WITH TIME ZONE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by    VARCHAR(120),
    updated_by    VARCHAR(120),
    version       BIGINT                   NOT NULL,
    CONSTRAINT pk_contacts PRIMARY KEY (id),
    CONSTRAINT uk_contacts_email UNIQUE (email),
    CONSTRAINT fk_contacts_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT ck_contacts_decision_role CHECK (decision_role IN ('DECISION_MAKER', 'INFLUENCER', 'TECHNICAL_EVALUATOR', 'USER', 'CHAMPION'))
);

CREATE INDEX idx_contacts_company ON contacts (company_id);
CREATE INDEX idx_contacts_name ON contacts (lower(first_name), lower(last_name));
CREATE UNIQUE INDEX uk_contacts_primary_per_company ON contacts (company_id) WHERE is_primary AND NOT archived;

CREATE TABLE contacts_aud (
    id            BIGINT   NOT NULL,
    rev           INTEGER  NOT NULL,
    revtype       SMALLINT,
    company_id    BIGINT,
    first_name    VARCHAR(80),
    last_name     VARCHAR(80),
    email         VARCHAR(160),
    phone         VARCHAR(20),
    mobile        VARCHAR(20),
    job_title     VARCHAR(100),
    department    VARCHAR(80),
    decision_role VARCHAR(30),
    is_primary    BOOLEAN,
    active        BOOLEAN,
    archived      BOOLEAN,
    archived_at   TIMESTAMP WITH TIME ZONE,
    created_at    TIMESTAMP WITH TIME ZONE,
    updated_at    TIMESTAMP WITH TIME ZONE,
    created_by    VARCHAR(120),
    updated_by    VARCHAR(120),
    CONSTRAINT pk_contacts_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_contacts_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);
