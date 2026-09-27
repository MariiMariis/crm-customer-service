"use client";

import { useCallback, useMemo, useState } from "react";
import { Pause, Play, RefreshCw } from "lucide-react";
import { api } from "@/lib/api";
import Button from "@/components/ui/Button";
import Badge from "@/components/ui/Badge";
import { ErrorBanner, Spinner } from "@/components/ui/Feedback";
import { PageHeader, Section } from "@/components/ui/Layout";
import { usePolling } from "@/components/platform/usePolling";
import { exchangeColor, serviceKey } from "@/components/platform/palette";
import ServiceCards from "@/components/platform/ServiceCards";
import TopologyMap from "@/components/platform/TopologyMap";
import QueuesTable from "@/components/platform/QueuesTable";
import EventFeed from "@/components/platform/EventFeed";

const REFRESH_MS = 3000;
const OVERALL = {
    UP: { label: "Todos os serviços operacionais", color: "emerald" },
    DEGRADED: { label: "Plataforma degradada", color: "amber" },
    DOWN: { label: "Plataforma fora do ar", color: "rose" },
};
const clock = new Intl.DateTimeFormat("pt-BR", { hour: "2-digit", minute: "2-digit", second: "2-digit" });

export default function PlatformPage() {
    const [paused, setPaused] = useState(false);
    const health = usePolling(() => api.platform.health(), REFRESH_MS, paused);

    const producers = useMemo(
        () => (health.data?.services ?? []).filter((service) => service.status === "UP" && service.messaging).map((service) => ({ name: service.name, exchange: service.messaging.exchange })),
        [health.data]
    );
    const producerKey = producers.map((producer) => producer.name).join(",");

    const loadEvents = useCallback(async () => {
        const results = await Promise.allSettled(
            producers.map((producer) =>
                api.platform.outbox(serviceKey(producer.name)).then((events) => events.map((event) => ({ ...event, service: producer.name, exchange: producer.exchange })))
            )
        );
        return results
            .filter((result) => result.status === "fulfilled")
            .flatMap((result) => result.value)
            .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
            .slice(0, 40);
    }, [producerKey]);

    const events = usePolling(loadEvents, REFRESH_MS, paused || producers.length === 0, [producerKey]);
    const overall = OVERALL[health.data?.status];

    const refreshAll = () => {
        health.reload();
        events.reload();
    };

    return (
        <>
            <PageHeader
                title="Plataforma"
                subtitle="Microsserviços, topologia do RabbitMQ, filas com retry e DLQ e eventos em tempo real."
                actions={
                    <>
                        <Button variant="secondary" size="sm" icon={paused ? Play : Pause} onClick={() => setPaused((current) => !current)}>
                            {paused ? "Retomar" : "Pausar"}
                        </Button>
                        <Button variant="secondary" size="sm" icon={RefreshCw} onClick={refreshAll}>Atualizar</Button>
                    </>
                }
            >
                <div className="mt-3 flex flex-wrap items-center gap-3">
                    {overall && <Badge color={overall.color}>{overall.label}</Badge>}
                    <span className="text-xs text-slate-500">
                        {paused ? "Atualização pausada" : `Atualiza a cada ${REFRESH_MS / 1000}s`}
                        {health.updatedAt && ` · última leitura às ${clock.format(health.updatedAt)}`}
                    </span>
                </div>
            </PageHeader>
            <ErrorBanner error={health.error} onRetry={health.reload} />
            {!health.data ? (
                health.loading && <Spinner label="Consultando o API Gateway..." />
            ) : (
                <div className="space-y-6">
                    <ServiceCards services={health.data.services} />
                    <Section
                        title="Topologia de mensageria"
                        actions={
                            <div className="hidden flex-wrap gap-3 md:flex">
                                {producers.map((producer) => (
                                    <span key={producer.name} className="inline-flex items-center gap-1.5 text-xs text-slate-400">
                                        <span className="h-2 w-2 rounded-full" style={{ backgroundColor: exchangeColor(producer.exchange) }} />
                                        {producer.exchange}
                                    </span>
                                ))}
                            </div>
                        }
                    >
                        <p className="mb-4 text-sm text-slate-400">
                            Cada serviço publica no próprio exchange do tipo topic; cada consumidor declara a sua fila e escolhe o que assinar pela routing key.
                            Passe o mouse sobre um exchange ou fila para destacar o caminho das mensagens.
                        </p>
                        <TopologyMap services={health.data.services} />
                    </Section>
                    <div className="grid grid-cols-1 gap-6 2xl:grid-cols-5">
                        <Section title="Filas de consumo" className="2xl:col-span-3" bodyClassName="">
                            <QueuesTable services={health.data.services} onReplayed={health.reload} />
                        </Section>
                        <Section title="Eventos ao vivo" className="2xl:col-span-2" bodyClassName="">
                            <ErrorBanner error={events.error} onRetry={events.reload} />
                            <EventFeed events={events.data} />
                        </Section>
                    </div>
                </div>
            )}
        </>
    );
}
