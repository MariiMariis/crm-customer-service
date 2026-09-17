package com.pb.notification.notification;

import com.pb.notification.config.NotificationProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationDispatcherTest {

    private static NotificationProperties properties(boolean auto) {
        return new NotificationProperties("PB CRM", "no-reply@pbcrm.local", 3,
                new NotificationProperties.Dispatch(auto, 5000, 20));
    }

    @Test
    void deveDespacharQuandoEnvioAutomaticoEstaHabilitado() {
        NotificationService service = mock(NotificationService.class);
        when(service.dispatchPending()).thenReturn(2);
        NotificationDispatcher dispatcher = new NotificationDispatcher(service, properties(true));

        dispatcher.dispatchPendingNotifications();

        verify(service).dispatchPending();
    }

    @Test
    void naoDeveDespacharQuandoEnvioAutomaticoEstaDesabilitado() {
        NotificationService service = mock(NotificationService.class);
        NotificationDispatcher dispatcher = new NotificationDispatcher(service, properties(false));

        dispatcher.dispatchPendingNotifications();

        verify(service, never()).dispatchPending();
        Mockito.verifyNoMoreInteractions(service);
    }
}
