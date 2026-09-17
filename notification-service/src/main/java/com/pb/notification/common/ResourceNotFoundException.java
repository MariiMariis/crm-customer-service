package com.pb.notification.common;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forId(String entityName, Object id) {
        return new ResourceNotFoundException("%s nao encontrado(a) para o id: %s".formatted(entityName, id));
    }
}
