CREATE SEQUENCE revinfo_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE revinfo (
    rev      INTEGER      NOT NULL,
    revtstmp BIGINT       NOT NULL,
    actor    VARCHAR(120) NOT NULL,
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

CREATE INDEX idx_revinfo_timestamp ON revinfo (revtstmp);

CREATE TABLE sales_rep_refs (
    id         BIGINT                   NOT NULL,
    name       VARCHAR(120)             NOT NULL,
    email      VARCHAR(160),
    manager_id BIGINT,
    active     BOOLEAN                  NOT NULL,
    archived   BOOLEAN                  NOT NULL,
    synced_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_sales_rep_refs PRIMARY KEY (id)
);

CREATE SEQUENCE leads_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE leads (
    id                             BIGINT                   NOT NULL,
    first_name                     VARCHAR(80)              NOT NULL,
    last_name                      VARCHAR(80)              NOT NULL,
    email                          VARCHAR(160),
    phone                          VARCHAR(20),
    company_name                   VARCHAR(160)             NOT NULL,
    job_title                      VARCHAR(100),
    source                         VARCHAR(20)              NOT NULL,
    status                         VARCHAR(20)              NOT NULL,
    score                          INTEGER                  NOT NULL,
    estimated_value                NUMERIC(15, 2),
    owner_id                       BIGINT,
    notes                          VARCHAR(2000),
    disqualify_reason              VARCHAR(500),
    conversion_cnpj                VARCHAR(18),
    conversion_industry            VARCHAR(40),
    conversion_company_size        VARCHAR(20),
    conversion_city                VARCHAR(100),
    conversion_state               VARCHAR(2),
    conversion_create_opportunity  BOOLEAN,
    conversion_opportunity_title   VARCHAR(160),
    conversion_expected_close_date DATE,
    conversion_requested_at        TIMESTAMP WITH TIME ZONE,
    conversion_failure_reason      VARCHAR(500),
    converted_company_id           BIGINT,
    converted_contact_id           BIGINT,
    converted_opportunity_id       BIGINT,
    converted_at                   TIMESTAMP WITH TIME ZONE,
    archived                       BOOLEAN                  NOT NULL DEFAULT FALSE,
    archived_at                    TIMESTAMP WITH TIME ZONE,
    created_at                     TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                     TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by                     VARCHAR(120),
    updated_by                     VARCHAR(120),
    version                        BIGINT                   NOT NULL,
    CONSTRAINT pk_leads PRIMARY KEY (id),
    CONSTRAINT ck_leads_source CHECK (source IN ('WEBSITE', 'REFERRAL', 'EVENT', 'COLD_CALL', 'LINKEDIN', 'PARTNER', 'CAMPAIGN')),
    CONSTRAINT ck_leads_status CHECK (status IN ('NEW', 'CONTACTED', 'QUALIFIED', 'CONVERTING', 'CONVERTED', 'UNQUALIFIED')),
    CONSTRAINT ck_leads_score CHECK (score BETWEEN 0 AND 100),
    CONSTRAINT ck_leads_contact CHECK (email IS NOT NULL OR phone IS NOT NULL),
    CONSTRAINT ck_leads_value CHECK (estimated_value IS NULL OR estimated_value >= 0)
);

CREATE INDEX idx_leads_status ON leads (status, archived);
CREATE INDEX idx_leads_owner ON leads (owner_id);
CREATE INDEX idx_leads_score ON leads (score DESC);
CREATE UNIQUE INDEX uk_leads_open_email ON leads (lower(email))
    WHERE email IS NOT NULL AND NOT archived AND status IN ('NEW', 'CONTACTED', 'QUALIFIED', 'CONVERTING');

CREATE TABLE leads_aud (
    id                             BIGINT   NOT NULL,
    rev                            INTEGER  NOT NULL,
    revtype                        SMALLINT,
    first_name                     VARCHAR(80),
    last_name                      VARCHAR(80),
    email                          VARCHAR(160),
    phone                          VARCHAR(20),
    company_name                   VARCHAR(160),
    job_title                      VARCHAR(100),
    source                         VARCHAR(20),
    status                         VARCHAR(20),
    score                          INTEGER,
    estimated_value                NUMERIC(15, 2),
    owner_id                       BIGINT,
    notes                          VARCHAR(2000),
    disqualify_reason              VARCHAR(500),
    conversion_cnpj                VARCHAR(18),
    conversion_industry            VARCHAR(40),
    conversion_company_size        VARCHAR(20),
    conversion_city                VARCHAR(100),
    conversion_state               VARCHAR(2),
    conversion_create_opportunity  BOOLEAN,
    conversion_opportunity_title   VARCHAR(160),
    conversion_expected_close_date DATE,
    conversion_requested_at        TIMESTAMP WITH TIME ZONE,
    conversion_failure_reason      VARCHAR(500),
    converted_company_id           BIGINT,
    converted_contact_id           BIGINT,
    converted_opportunity_id       BIGINT,
    converted_at                   TIMESTAMP WITH TIME ZONE,
    archived                       BOOLEAN,
    archived_at                    TIMESTAMP WITH TIME ZONE,
    created_at                     TIMESTAMP WITH TIME ZONE,
    updated_at                     TIMESTAMP WITH TIME ZONE,
    created_by                     VARCHAR(120),
    updated_by                     VARCHAR(120),
    CONSTRAINT pk_leads_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_leads_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);
