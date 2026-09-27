CREATE SEQUENCE revinfo_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE revinfo (
    rev      INTEGER      NOT NULL,
    revtstmp BIGINT       NOT NULL,
    actor    VARCHAR(120) NOT NULL,
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

CREATE INDEX idx_revinfo_timestamp ON revinfo (revtstmp);

CREATE SEQUENCE sales_reps_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE sales_reps (
    id            BIGINT                   NOT NULL,
    name          VARCHAR(120)             NOT NULL,
    email         VARCHAR(160)             NOT NULL,
    phone         VARCHAR(20),
    team          VARCHAR(30)              NOT NULL,
    role          VARCHAR(20)              NOT NULL,
    manager_id    BIGINT,
    monthly_quota NUMERIC(15, 2),
    active        BOOLEAN                  NOT NULL,
    archived      BOOLEAN                  NOT NULL DEFAULT FALSE,
    archived_at   TIMESTAMP WITH TIME ZONE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by    VARCHAR(120),
    updated_by    VARCHAR(120),
    version       BIGINT                   NOT NULL,
    CONSTRAINT pk_sales_reps PRIMARY KEY (id),
    CONSTRAINT uk_sales_reps_email UNIQUE (email),
    CONSTRAINT fk_sales_reps_manager FOREIGN KEY (manager_id) REFERENCES sales_reps (id),
    CONSTRAINT ck_sales_reps_team CHECK (team IN ('INSIDE_SALES', 'FIELD_SALES', 'PRE_SALES', 'ACCOUNT_MANAGEMENT')),
    CONSTRAINT ck_sales_reps_role CHECK (role IN ('REP', 'MANAGER')),
    CONSTRAINT ck_sales_reps_quota CHECK (monthly_quota IS NULL OR monthly_quota >= 0)
);

CREATE INDEX idx_sales_reps_team ON sales_reps (team);
CREATE INDEX idx_sales_reps_manager ON sales_reps (manager_id);
CREATE INDEX idx_sales_reps_active_archived ON sales_reps (active, archived);

CREATE TABLE sales_reps_aud (
    id            BIGINT   NOT NULL,
    rev           INTEGER  NOT NULL,
    revtype       SMALLINT,
    name          VARCHAR(120),
    email         VARCHAR(160),
    phone         VARCHAR(20),
    team          VARCHAR(30),
    role          VARCHAR(20),
    manager_id    BIGINT,
    monthly_quota NUMERIC(15, 2),
    active        BOOLEAN,
    archived      BOOLEAN,
    archived_at   TIMESTAMP WITH TIME ZONE,
    created_at    TIMESTAMP WITH TIME ZONE,
    updated_at    TIMESTAMP WITH TIME ZONE,
    created_by    VARCHAR(120),
    updated_by    VARCHAR(120),
    CONSTRAINT pk_sales_reps_aud PRIMARY KEY (rev, id),
    CONSTRAINT fk_sales_reps_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (rev)
);
