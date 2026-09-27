package com.pb.crm.commons.actor;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class RequestActor {

    public static final String HEADER = "X-Actor";
    public static final String SYSTEM = "system";

    private RequestActor() {
    }

    public static String current() {
        return MessageContext.current()
                .map(MessageContext.Current::actor)
                .filter(actor -> !actor.isBlank())
                .orElseGet(RequestActor::fromHttpRequest);
    }

    private static String fromHttpRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            String header = servletAttributes.getRequest().getHeader(HEADER);
            if (header != null && !header.isBlank()) {
                return header.trim();
            }
        }
        return SYSTEM;
    }
}
