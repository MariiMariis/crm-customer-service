"use client";

import { useMemo, useState } from "react";
import { exchangeColor } from "@/components/platform/palette";

const WIDTH = 1000;
const EXCHANGE_X = 20;
const EXCHANGE_WIDTH = 240;
const EXCHANGE_HEIGHT = 46;
const QUEUE_X = 610;
const QUEUE_WIDTH = 370;
const QUEUE_HEIGHT = 30;
const QUEUE_GAP = 8;
const GROUP_HEADER = 26;
const GROUP_GAP = 16;
const TOP = 10;

function buildLayout(services) {
    const producers = new Map();
    services.forEach((service) => {
        if (service.messaging?.exchange) producers.set(service.messaging.exchange, service.name);
    });
    const groups = [];
    let y = TOP;
    services.forEach((service) => {
        const queues = service.messaging?.queues ?? [];
        if (queues.length === 0) return;
        const header = y;
        y += GROUP_HEADER;
        const nodes = queues.map((queue) => {
            const node = { ...queue, service: service.name, y };
            y += QUEUE_HEIGHT + QUEUE_GAP;
            return node;
        });
        groups.push({ service: service.name, header, nodes });
        y += GROUP_GAP;
    });
    groups.flatMap((group) => group.nodes).forEach((node) => {
        node.subscriptions?.forEach((subscription) => {
            if (!producers.has(subscription.exchange)) producers.set(subscription.exchange, null);
        });
    });
    const height = Math.max(y, TOP + producers.size * (EXCHANGE_HEIGHT + 24));
    const exchangeNames = [...producers.keys()];
    const slot = height / Math.max(exchangeNames.length, 1);
    const exchanges = exchangeNames.map((name, index) => ({
        name,
        producer: producers.get(name),
        y: slot * index + (slot - EXCHANGE_HEIGHT) / 2,
    }));
    const byName = Object.fromEntries(exchanges.map((exchange) => [exchange.name, exchange]));
    const links = groups
        .flatMap((group) => group.nodes)
        .flatMap((node) =>
            (node.subscriptions ?? []).map((subscription) => ({
                id: `${subscription.exchange}->${node.name}:${subscription.routingKey}`,
                exchange: byName[subscription.exchange],
                queue: node,
                routingKey: subscription.routingKey,
            }))
        );
    return { exchanges, groups, links, height };
}

function linkPath(link, index, total) {
    const spread = total > 1 ? (index / (total - 1) - 0.5) * 24 : 0;
    const x1 = EXCHANGE_X + EXCHANGE_WIDTH;
    const y1 = link.exchange.y + EXCHANGE_HEIGHT / 2 + spread;
    const x2 = QUEUE_X;
    const y2 = link.queue.y + QUEUE_HEIGHT / 2;
    const middle = (x1 + x2) / 2;
    return `M ${x1} ${y1} C ${middle} ${y1}, ${middle} ${y2}, ${x2} ${y2}`;
}

export default function TopologyMap({ services }) {
    const [focus, setFocus] = useState(null);
    const layout = useMemo(() => buildLayout(services), [services]);

    const isActive = (link) => !focus || focus === link.exchange.name || focus === link.queue.name;
    const nodeActive = (name) => !focus || focus === name || layout.links.some((link) => isActive(link) && (link.exchange.name === name || link.queue.name === name));
    const linksByExchange = layout.links.reduce((map, link) => {
        (map[link.exchange.name] ??= []).push(link);
        return map;
    }, {});

    if (layout.links.length === 0) {
        return <p className="py-10 text-center text-sm text-slate-400">Topologia indisponível: nenhum serviço respondeu com as filas.</p>;
    }

    return (
        <div className="overflow-x-auto">
            <svg viewBox={`0 0 ${WIDTH} ${layout.height}`} className="min-w-[760px]" role="img" aria-label="Topologia de exchanges e filas do RabbitMQ">
                {layout.links.map((link) => {
                    const siblings = linksByExchange[link.exchange.name];
                    const color = exchangeColor(link.exchange.name);
                    return (
                        <path
                            key={link.id}
                            d={linkPath(link, siblings.indexOf(link), siblings.length)}
                            fill="none"
                            stroke={color}
                            strokeWidth={focus && isActive(link) ? 2.4 : 1.4}
                            strokeOpacity={isActive(link) ? 0.85 : 0.08}
                            className="transition-all"
                        >
                            <title>{`${link.exchange.name} → ${link.queue.name} (routing key ${link.routingKey})`}</title>
                        </path>
                    );
                })}
                {layout.exchanges.map((exchange) => {
                    const color = exchangeColor(exchange.name);
                    return (
                        <g
                            key={exchange.name}
                            onMouseEnter={() => setFocus(exchange.name)}
                            onMouseLeave={() => setFocus(null)}
                            opacity={nodeActive(exchange.name) ? 1 : 0.3}
                            className="cursor-pointer transition-opacity"
                        >
                            <rect x={EXCHANGE_X} y={exchange.y} width={EXCHANGE_WIDTH} height={EXCHANGE_HEIGHT} rx="10" fill="#0f172a" stroke={color} strokeWidth="1.5" />
                            <rect x={EXCHANGE_X} y={exchange.y} width="6" height={EXCHANGE_HEIGHT} rx="3" fill={color} />
                            <text x={EXCHANGE_X + 18} y={exchange.y + 20} fill="#f8fafc" fontSize="14" fontWeight="600">{exchange.name}</text>
                            <text x={EXCHANGE_X + 18} y={exchange.y + 36} fill="#94a3b8" fontSize="11">
                                {exchange.producer ? `topic · publicado por ${exchange.producer}` : "topic"}
                            </text>
                        </g>
                    );
                })}
                {layout.groups.map((group) => (
                    <g key={group.service}>
                        <text x={QUEUE_X} y={group.header + 16} fill="#64748b" fontSize="11" fontWeight="600" letterSpacing="0.06em">
                            {`CONSUMIDOR · ${group.service.toUpperCase()}`}
                        </text>
                        {group.nodes.map((queue) => {
                            const alert = queue.deadLetters > 0 ? "#f43f5e" : queue.retrying > 0 || queue.messages > 0 ? "#f59e0b" : "#334155";
                            const summary = [
                                queue.messages > 0 && `${queue.messages} msg`,
                                queue.retrying > 0 && `${queue.retrying} retry`,
                                queue.deadLetters > 0 && `${queue.deadLetters} DLQ`,
                            ].filter(Boolean).join(" · ") || `${queue.consumers ?? 0} consumidor(es)`;
                            return (
                                <g
                                    key={queue.name}
                                    onMouseEnter={() => setFocus(queue.name)}
                                    onMouseLeave={() => setFocus(null)}
                                    opacity={nodeActive(queue.name) ? 1 : 0.3}
                                    className="cursor-pointer transition-opacity"
                                >
                                    <rect x={QUEUE_X} y={queue.y} width={QUEUE_WIDTH} height={QUEUE_HEIGHT} rx="8" fill="#111827" stroke={alert} strokeWidth="1.2" />
                                    <text x={QUEUE_X + 12} y={queue.y + 19} fill="#e2e8f0" fontSize="12" fontFamily="ui-monospace, monospace">{queue.name}</text>
                                    <text x={QUEUE_X + QUEUE_WIDTH - 12} y={queue.y + 19} fill={alert === "#334155" ? "#64748b" : alert} fontSize="11" textAnchor="end">{summary}</text>
                                </g>
                            );
                        })}
                    </g>
                ))}
            </svg>
        </div>
    );
}
