package com.pb.crm.messagingtest;

import com.pb.crm.commons.actor.RequestActor;
import com.pb.crm.commons.messaging.EventEnvelope;
import com.pb.crm.commons.messaging.consumer.EventConsumer;
import com.pb.crm.commons.messaging.topology.ConsumerQueue;
import com.pb.crm.commons.messaging.topology.MessagingTopology;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class MessagingTestApplication {

    public static final String QUEUE = "commons-test.recording";
    public static final String CONSUMER = "recording-consumer";

    @Bean
    public Declarables recordingQueue(MessagingTopology topology) {
        return topology.consumer(ConsumerQueue.of(QUEUE, ConsumerQueue.on("test.events", "test.#")));
    }

    @Component
    public static class RecordingListener {

        private final EventConsumer eventConsumer;
        private final List<EventEnvelope> received = new CopyOnWriteArrayList<>();
        private final List<String> actors = new CopyOnWriteArrayList<>();
        private final AtomicInteger attempts = new AtomicInteger();
        private volatile boolean failing;

        public RecordingListener(EventConsumer eventConsumer) {
            this.eventConsumer = eventConsumer;
        }

        @RabbitListener(queues = QUEUE)
        public void onMessage(Message message) {
            eventConsumer.consume(message, CONSUMER, envelope -> {
                attempts.incrementAndGet();
                if (failing) {
                    throw new IllegalStateException("falha simulada no consumidor");
                }
                received.add(envelope);
                actors.add(RequestActor.current());
            });
        }

        public List<EventEnvelope> received() {
            return received;
        }

        public List<String> actors() {
            return actors;
        }

        public int attempts() {
            return attempts.get();
        }

        public void failing(boolean value) {
            this.failing = value;
        }

        public void reset() {
            received.clear();
            actors.clear();
            attempts.set(0);
            failing = false;
        }
    }
}
