CREATE SEQUENCE revinfo_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE revinfo (
    rev      INTEGER      NOT NULL,
    revtstmp BIGINT       NOT NULL,
    actor    VARCHAR(120) NOT NULL,
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

CREATE TABLE recipients (
    id            BIGINT                   NOT NULL,
    name          VARCHAR(120)             NOT NULL,
    email         VARCHAR(160),
    manager_id    BIGINT,
    active        BOOLEAN                  NOT NULL,
    archived      BOOLEAN                  NOT NULL,
    synced_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    last_event_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_recipients PRIMARY KEY (id)
);

CREATE TABLE notification_preferences (
    sales_rep_id  BIGINT                   NOT NULL,
    email_enabled BOOLEAN                  NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_notification_preferences PRIMARY KEY (sales_rep_id)
);

CREATE SEQUENCE notifications_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE notifications (
    id              BIGINT                   NOT NULL,
    recipient_id    BIGINT                   NOT NULL,
    recipient_name  VARCHAR(120)             NOT NULL,
    recipient_email VARCHAR(160),
    channel         VARCHAR(20)              NOT NULL,
    type            VARCHAR(40)              NOT NULL,
    title           VARCHAR(160)             NOT NULL,
    message         VARCHAR(2000)            NOT NULL,
    link            VARCHAR(200),
    source_event_id UUID                     NOT NULL,
    status          VARCHAR(20)              NOT NULL,
    skip_reason     VARCHAR(300),
    sent_at         TIMESTAMP WITH TIME ZONE,
    read_at         TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120),
    version         BIGINT                   NOT NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT uk_notifications_event_recipient_channel UNIQUE (source_event_id, recipient_id, channel),
    CONSTRAINT ck_notifications_channel CHECK (channel IN ('IN_APP', 'EMAIL')),
    CONSTRAINT ck_notifications_status CHECK (status IN ('PENDING', 'SENT', 'SKIPPED')),
    CONSTRAINT ck_notifications_type CHECK (type IN ('LEAD_ASSIGNED', 'DISCOUNT_APPROVAL_REQUESTED', 'OPPORTUNITY_WON', 'OPPORTUNITY_LOST'))
);

CREATE INDEX idx_notifications_inbox ON notifications (recipient_id, channel, read_at, created_at DESC);
CREATE INDEX idx_notifications_status ON notifications (status);

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
