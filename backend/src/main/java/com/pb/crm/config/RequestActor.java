package com.pb.crm.config;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class RequestActor {

    public static final String HEADER = "X-Actor";
    public static final String SYSTEM = "system";

    private RequestActor() {
    }

    public static String current() {
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
