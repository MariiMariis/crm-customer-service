package com.pb.crm.notification;

public class NotificationServiceUnavailableException extends RuntimeException {

    public NotificationServiceUnavailableException(Throwable cause) {
        super("o servico de notificacoes esta indisponivel no momento. Tente novamente mais tarde", cause);
    }

    public NotificationServiceUnavailableException(String message) {
        super(message);
    }
}
