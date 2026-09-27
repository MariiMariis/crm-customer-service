CREATE SEQUENCE revinfo_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE revinfo (
    rev      INTEGER      NOT NULL,
    revtstmp BIGINT       NOT NULL,
    actor    VARCHAR(120) NOT NULL,
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

CREATE TABLE outbox_events (
    id             UUID                     NOT NULL,
    exchange       VARCHAR(120)             NOT NULL,
    routing_key    VARCHAR(160)             NOT NULL,
    event_type     VARCHAR(160)             NOT NULL,
    aggregate_type VARCHAR(80)              NOT NULL,
    aggregate_id   VARCHAR(80)              NOT NULL,
    envelope       TEXT                     NOT NULL,
    status         VARCHAR(20)              NOT NULL,
    attempts       INTEGER                  NOT NULL,
    last_error     VARCHAR(1000),
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at   TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_outbox_events PRIMARY KEY (id)
);

CREATE INDEX idx_outbox_events_pending ON outbox_events (status, created_at);

CREATE TABLE processed_events (
    consumer     VARCHAR(120)             NOT NULL,
    event_id     UUID                     NOT NULL,
    event_type   VARCHAR(160)             NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_processed_events PRIMARY KEY (consumer, event_id)
);
