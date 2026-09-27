"use client";

import Link from "next/link";
import { useEffect, useRef, useState } from "react";
import { Radio } from "lucide-react";
import { exchangeColor } from "@/components/platform/palette";
import Badge from "@/components/ui/Badge";
import { EmptyState } from "@/components/ui/Feedback";

const AGGREGATE_LINKS = {
    Company: (id) => `/companies/${id}`,
    Lead: (id) => `/leads/${id}`,
    Opportunity: (id) => `/opportunities/${id}`,
};

const clock = new Intl.DateTimeFormat("pt-BR", { hour: "2-digit", minute: "2-digit", second: "2-digit" });

function publishDelay(event) {
    if (!event.publishedAt) return null;
    const millis = new Date(event.publishedAt) - new Date(event.createdAt);
    return millis < 1000 ? `${Math.max(millis, 0)} ms` : `${(millis / 1000).toFixed(1)} s`;
}

export default function EventFeed({ events }) {
    const seen = useRef(null);
    const [fresh, setFresh] = useState(new Set());

    useEffect(() => {
        if (!events) return;
        const ids = events.map((event) => event.id);
        if (seen.current) {
            setFresh(new Set(ids.filter((id) => !seen.current.has(id))));
        }
        seen.current = new Set([...(seen.current ?? []), ...ids]);
    }, [events]);

    if (!events?.length) {
        return <EmptyState icon={Radio} title="Nenhum evento ainda" description="Crie ou altere um registro e acompanhe o evento sair do outbox para o RabbitMQ." />;
    }

    return (
        <ul className="max-h-[560px] divide-y divide-surface-800 overflow-y-auto">
            {events.map((event) => {
                const link = AGGREGATE_LINKS[event.aggregateType]?.(event.aggregateId);
                const delay = publishDelay(event);
                return (
                    <li key={event.id} className={`flex items-start gap-3 px-5 py-3 ${fresh.has(event.id) ? "event-arrived" : ""}`}>
                        <span className="mt-1.5 h-2 w-2 shrink-0 rounded-full" style={{ backgroundColor: exchangeColor(event.exchange) }} />
                        <div className="min-w-0 flex-1">
                            <div className="flex flex-wrap items-center gap-2">
                                <code className="text-xs font-semibold text-white">{event.eventType}</code>
                                {event.status === "PENDING" ? <Badge color="amber">No outbox</Badge> : <Badge color="emerald">Publicado</Badge>}
                                {event.attempts > 1 && <Badge color="rose">{event.attempts} tentativas</Badge>}
                            </div>
                            <p className="mt-0.5 text-xs text-slate-400">
                                {event.service} ·{" "}
                                {link ? (
                                    <Link href={link} className="text-brand-300 hover:underline">{event.aggregateType} #{event.aggregateId}</Link>
                                ) : (
                                    <span>{event.aggregateType} #{event.aggregateId}</span>
                                )}
                                {delay && <span className="text-slate-500"> · publicado em {delay}</span>}
                            </p>
                            {event.lastError && <p className="mt-0.5 text-xs text-rose-300">{event.lastError}</p>}
                        </div>
                        <time className="shrink-0 font-mono text-xs text-slate-500">{clock.format(new Date(event.createdAt))}</time>
                    </li>
                );
            })}
        </ul>
    );
}
