package com.pb.crm.commons.messaging;

public final class MessageHeaders {

    public static final String EVENT_TYPE = "x-event-type";
    public static final String SOURCE = "x-source";
    public static final String AGGREGATE_TYPE = "x-aggregate-type";
    public static final String AGGREGATE_ID = "x-aggregate-id";
    public static final String ACTOR = "x-actor";
    public static final String RETRY_COUNT = "x-retry-count";
    public static final String ORIGINAL_QUEUE = "x-original-queue";
    public static final String EXCEPTION_CLASS = "x-exception-class";
    public static final String EXCEPTION_MESSAGE = "x-exception-message";
    public static final String FAILED_AT = "x-failed-at";

    private MessageHeaders() {
    }
}
