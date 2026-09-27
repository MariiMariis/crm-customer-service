"use client";

import { useState } from "react";
import { RotateCcw } from "lucide-react";
import { api } from "@/lib/api";
import { number } from "@/lib/format";
import { useToast } from "@/components/providers/ToastProvider";
import Button from "@/components/ui/Button";
import { exchangeColor, serviceKey } from "@/components/platform/palette";

function Count({ value, alert }) {
    if (value === null || value === undefined) return <span className="text-slate-600">-</span>;
    return <span className={value > 0 ? alert : "text-slate-400"}>{number(value)}</span>;
}

export default function QueuesTable({ services, onReplayed }) {
    const toast = useToast();
    const [replaying, setReplaying] = useState(null);
    const rows = services.flatMap((service) => (service.messaging?.queues ?? []).map((queue) => ({ ...queue, service: service.name })));

    const replay = async (row) => {
        setReplaying(row.name);
        try {
            const result = await api.platform.replay(serviceKey(row.service), row.name);
            toast.success(`${result.replayed} mensagem(ns) devolvida(s) para ${row.name}`);
            onReplayed();
        } catch (err) {
            toast.error(err);
        } finally {
            setReplaying(null);
        }
    };

    return (
        <div className="overflow-x-auto">
            <table className="w-full text-sm">
                <thead>
                    <tr className="table-head">
                        <th className="px-5 py-3">Fila</th>
                        <th className="px-5 py-3">Assinaturas</th>
                        <th className="whitespace-nowrap px-4 py-3 text-right">Prontas</th>
                        <th className="whitespace-nowrap px-4 py-3 text-right">Consumidores</th>
                        <th className="whitespace-nowrap px-4 py-3 text-right">Em retry</th>
                        <th className="whitespace-nowrap px-4 py-3 text-right">DLQ</th>
                        <th className="px-4 py-3" />
                    </tr>
                </thead>
                <tbody>
                    {rows.map((row) => (
                        <tr key={row.name} className="table-row">
                            <td className="px-5 py-3">
                                <p className="font-mono text-xs text-white">{row.name}</p>
                                <p className="text-xs text-slate-500">{row.service}</p>
                            </td>
                            <td className="px-5 py-3">
                                <div className="flex max-w-[240px] flex-wrap gap-1">
                                    {(row.subscriptions ?? []).map((subscription) => (
                                        <span key={`${subscription.exchange}:${subscription.routingKey}`} title={`exchange ${subscription.exchange} · routing key ${subscription.routingKey}`} className="inline-flex items-center gap-1.5 whitespace-nowrap rounded-md bg-surface-800 px-2 py-0.5 font-mono text-[11px] text-slate-300">
                                            <span className="h-1.5 w-1.5 rounded-full" style={{ backgroundColor: exchangeColor(subscription.exchange) }} />
                                            {subscription.routingKey}
                                        </span>
                                    ))}
                                </div>
                            </td>
                            <td className="px-4 py-3 text-right"><Count value={row.messages} alert="text-amber-300" /></td>
                            <td className="px-4 py-3 text-right"><Count value={row.consumers} alert="text-slate-200" /></td>
                            <td className="px-4 py-3 text-right"><Count value={row.retrying} alert="text-amber-300" /></td>
                            <td className="px-4 py-3 text-right"><Count value={row.deadLetters} alert="font-semibold text-rose-300" /></td>
                            <td className="px-4 py-3 text-right">
                                {row.deadLetters > 0 && (
                                    <Button size="sm" variant="secondary" icon={RotateCcw} loading={replaying === row.name} onClick={() => replay(row)} title="Reprocessar DLQ" aria-label={`Reprocessar DLQ de ${row.name}`} />
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}
