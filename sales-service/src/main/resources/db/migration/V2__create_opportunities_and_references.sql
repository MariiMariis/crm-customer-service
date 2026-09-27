CREATE TABLE company_refs (
    id           BIGINT                   NOT NULL,
    display_name VARCHAR(160)             NOT NULL,
    cnpj         VARCHAR(14),
    owner_id     BIGINT,
    archived     BOOLEAN                  NOT NULL,
    synced_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_company_refs PRIMARY KEY (id)
);

CREATE TABLE contact_refs (
    id         BIGINT                   NOT NULL,
    company_id BIGINT                   NOT NULL,
    full_name  VARCHAR(160)             NOT NULL,
    email      VARCHAR(160),
    active     BOOLEAN                  NOT NULL,
    archived   BOOLEAN                  NOT NULL,
    synced_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_contact_refs PRIMARY KEY (id)
);

CREATE INDEX idx_contact_refs_company ON contact_refs (company_id);

CREATE TABLE product_refs (
    id                   BIGINT                   NOT NULL,
    sku                  VARCHAR(20)              NOT NULL,
    name                 VARCHAR(160)             NOT NULL,
    category             VARCHAR(20)              NOT NULL,
    billing              VARCHAR(20)              NOT NULL,
    unit_price           NUMERIC(15, 2)           NOT NULL,
    max_discount_percent NUMERIC(5, 2)            NOT NULL,
    active               BOOLEAN                  NOT NULL,
    archived             BOOLEAN                  NOT NULL,
    synced_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_product_refs PRIMARY KEY (id)
);

CREATE SEQUENCE opportunities_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE opportunities (
    id                      BIGINT                   NOT NULL,
    title                   VARCHAR(160)             NOT NULL,
    description             VARCHAR(2000),
    company_id              BIGINT                   NOT NULL,
    contact_id              BIGINT,
    owner_id                BIGINT                   NOT NULL,
    lead_id                 BIGINT,
    stage                   VARCHAR(20)              NOT NULL,
    probability             INTEGER                  NOT NULL,
    expected_close_date     DATE                     NOT NULL,
    contract_term_months    INTEGER                  NOT NULL,
    estimated_value         NUMERIC(17, 2),
    one_time_value          NUMERIC(17, 2)           NOT NULL,
    monthly_recurring_value NUMERIC(17, 2)           NOT NULL,
    amount                  NUMERIC(17, 2)           NOT NULL,
    weighted_amount         NUMERIC(17, 2)           NOT NULL,
    loss_reason             VARCHAR(500),
    closed_at               TIMESTAMP WITH TIME ZONE,
    discount_approval       VARCHAR(20)              NOT NULL,
    approval_decided_by     BIGINT,
    approval_comment        VARCHAR(500),
    approval_decided_at     TIMESTAMP WITH TIME ZONE,
    archived                BOOLEAN                  NOT NULL DEFAULT FALSE,
    archived_at             TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by              VARCHAR(120),
    updated_by              VARCHAR(120),
    version                 BIGINT                   NOT NULL,
    CONSTRAINT pk_opportunities PRIMARY KEY (id),
    CONSTRAINT fk_opportunities_lead FOREIGN KEY (lead_id) REFERENCES leads (id),
    CONSTRAINT ck_opportunities_stage CHECK (stage IN ('PROSPECTING', 'QUALIFICATION', 'PROPOSAL', 'NEGOTIATION', 'WON', 'LOST')),
    CONSTRAINT ck_opportunities_probability CHECK (probability BETWEEN 0 AND 100),
    CONSTRAINT ck_opportunities_term CHECK (contract_term_months BETWEEN 1 AND 60),
    CONSTRAINT ck_opportunities_approval CHECK (discount_approval IN ('NOT_REQUIRED', 'PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT ck_opportunities_lost_reason CHECK (stage <> 'LOST' OR loss_reason IS NOT NULL)
);

CREATE INDEX idx_opportunities_stage ON opportunities (stage, archived);
CREATE INDEX idx_opportunities_owner ON opportunities (owner_id);
CREATE INDEX idx_opportunities_company ON opportunities (company_id);
CREATE INDEX idx_opportunities_close_date ON opportunities (expected_close_date);

CREATE SEQUENCE opportunity_items_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE opportunity_items (
    id                   BIGINT         NOT NULL,
    opportunity_id       BIGINT         NOT NULL,
    product_id           BIGINT         NOT NULL,
    sku                  VARCHAR(20)    NOT NULL,
    product_name         VARCHAR(160)   NOT NULL,
    category             VARCHAR(20)    NOT NULL,
    billing              VARCHAR(20)    NOT NULL,
    unit_price           NUMERIC(15, 2) NOT NULL,
    max_discount_percent NUMERIC(5, 2)  NOT NULL,
    quantity             INTEGER        NOT NULL,
    discount_percent     NUMERIC(5, 2)  NOT NULL,
    net_total            NUMERIC(17, 2) NOT NULL,
    CONSTRAINT pk_opportunity_items PRIMARY KEY (id),
    CONSTRAINT fk_opportunity_items_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities (id),
    CONSTRAINT ck_opportunity_items_quantity CHECK (quantity > 0),
    CONSTRAINT ck_opportunity_items_discount CHECK (discount_percent BETWEEN 0 AND 100)
);

CREATE INDEX idx_opportunity_items_opportunity ON opportunity_items (opportunity_id);

CREATE SEQUENCE opportunity_stage_history_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE opportunity_stage_history (
    id             BIGINT                   NOT NULL,
    opportunity_id BIGINT                   NOT NULL,
    from_stage     VARCHAR(20),
    to_stage       VARCHAR(20)              NOT NULL,
    probability    INTEGER                  NOT NULL,
    reason         VARCHAR(500),
    changed_by     VARCHAR(120),
    changed_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_opportunity_stage_history PRIMARY KEY (id),
    CONSTRAINT fk_stage_history_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities (id)
);

CREATE INDEX idx_stage_history_opportunity ON opportunity_stage_history (opportunity_id, changed_at);

CREATE TABLE opportunities_aud (
    id                      BIGINT   NOT NULL,
    rev                     INTEGER  NOT NULL,
    revtype                 SMALLINT,
    title                   VARCHAR(160),
    description             VARCHAR(2000),
    company_id              BIGINT,
    contact_id              BIGINT,
    owner_id                BIGINT,
    lead_id                 BIGINT,
    stage                   VARCHAR(20),
    probability             INTEGER,
    expected_close_date     DATE,
    contract_term_months    INTEGER,
    estimated_value         NUMERIC(17, 2),
    one_time_value          NUMERIC(17, 2),
    monthly_recurring_value NUMERIC(17, 2),
    amount                  NUMERIC(17, 2),
    weighted_amount         NUMERIC(17, 2),
    loss_reason             VARCHAR(500),
    closed_at               TIMESTAMP WITH TIME ZONE,
    discount_approval       VARCHAR(20),
    approval_decided_by     BIGINT,
    approval_comment        VARCHAR(500),
    approval_decided_at     TIMESTAMP WITH TIME ZONE,
    archived                BOOLEAN,
    archived_at             TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE,
    updated_at              TIMESTAMP WITH TIME ZONE,
    created_by              VARCHAR(120),
    updated_by              VARCHAR(120),
    CONSTRAINT pk_opportunities_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_opportunities_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);
