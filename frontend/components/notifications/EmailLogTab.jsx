"use client";

import Link from "next/link";
import { useState } from "react";
import { CheckCircle2, Clock, Mail, MailX } from "lucide-react";
import { api } from "@/lib/api";
import { useApi } from "@/lib/useApi";
import { NOTIFICATION_STATUSES, NOTIFICATION_TYPES, labelOf } from "@/lib/labels";
import { dateTime, number } from "@/lib/format";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import { LabeledBadge } from "@/components/ui/Badge";
import { Select } from "@/components/ui/Field";
import { EmptyState, ErrorBanner, Spinner } from "@/components/ui/Feedback";
import { KpiCard, Pagination } from "@/components/ui/Layout";

function deliveryTime(notification) {
    if (!notification.sentAt) return null;
    const millis = new Date(notification.sentAt) - new Date(notification.createdAt);
    return millis < 1000 ? `${Math.max(millis, 0)} ms` : `${(millis / 1000).toFixed(1)} s`;
}

export default function EmailLogTab() {
    const { activeSalesReps } = useCurrentUser();
    const [filters, setFilters] = useState({ recipientId: "", status: "", type: "" });
    const [page, setPage] = useState(0);
    const { data, loading, error, reload } = useApi(
        () => api.notifications.list({ channel: "EMAIL", ...filters, page, pageSize: 15 }),
        [filters, page]
    );
    const { data: stats } = useApi(async () => {
        const count = (status) => api.notifications.list({ channel: "EMAIL", status, pageSize: 1 }).then((result) => result.totalElements);
        const [total, sent, pending, skipped] = await Promise.all([count(), count("SENT"), count("PENDING"), count("SKIPPED")]);
        return { total, sent, pending, skipped };
    }, []);
    const setFilter = (field) => (event) => {
        setPage(0);
        setFilters((current) => ({ ...current, [field]: event.target.value }));
    };

    return (
        <div className="space-y-5">
            <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
                <KpiCard label="E-mails gerados" value={number(stats?.total)} hint="Toda a equipe" icon={Mail} />
                <KpiCard label="Enviados" value={number(stats?.sent)} hint="Entregues pelo worker" icon={CheckCircle2} tone="emerald" />
                <KpiCard label="Na fila" value={number(stats?.pending)} hint="Em processamento, retry ou DLQ" icon={Clock} tone="amber" />
                <KpiCard label="Não enviados" value={number(stats?.skipped)} hint="Preferência desligada ou sem e-mail" icon={MailX} tone="rose" />
            </div>
            <p className="text-sm text-slate-400">
                Cada alerta vira uma mensagem na fila de trabalho <code className="rounded bg-surface-800 px-1.5 py-0.5 text-xs text-brand-300">notification.dispatch</code>,
                consumida por vários workers em paralelo. Falhas passam por retry com atraso e, esgotadas as tentativas, vão para a DLQ, que pode ser
                reprocessada em <Link href="/platform" className="text-brand-300 hover:underline">Plataforma</Link>.
            </p>
            <div className="card">
                <div className="grid grid-cols-1 gap-3 border-b border-surface-700/70 p-4 sm:grid-cols-3">
                    <Select options={activeSalesReps.map((rep) => ({ value: rep.id, label: rep.name }))} placeholder="Todos os destinatários" value={filters.recipientId} onChange={setFilter("recipientId")} />
                    <Select options={NOTIFICATION_STATUSES} placeholder="Todos os status" value={filters.status} onChange={setFilter("status")} />
                    <Select options={NOTIFICATION_TYPES} placeholder="Todos os tipos" value={filters.type} onChange={setFilter("type")} />
                </div>
                <ErrorBanner error={error} onRetry={reload} />
                {loading && !data ? (
                    <Spinner />
                ) : data?.content.length === 0 ? (
                    <EmptyState icon={Mail} title="Nenhum e-mail encontrado" />
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead>
                                <tr className="table-head">
                                    <th className="px-5 py-3">Destinatário</th>
                                    <th className="px-5 py-3">Assunto</th>
                                    <th className="px-5 py-3">Status</th>
                                    <th className="px-5 py-3">Gerado em</th>
                                    <th className="px-5 py-3 text-right">Entrega</th>
                                </tr>
                            </thead>
                            <tbody>
                                {data?.content.map((notification) => (
                                    <tr key={notification.id} className="table-row">
                                        <td className="px-5 py-3">
                                            <p className="font-medium text-white">{notification.recipientName}</p>
                                            <p className="text-xs text-slate-500">{notification.recipientEmail ?? "sem e-mail"}</p>
                                        </td>
                                        <td className="px-5 py-3">
                                            <p className="text-slate-200">{notification.title}</p>
                                            <p className="text-xs text-slate-500">{labelOf(NOTIFICATION_TYPES, notification.type)}</p>
                                        </td>
                                        <td className="px-5 py-3">
                                            <LabeledBadge list={NOTIFICATION_STATUSES} value={notification.status} />
                                            {notification.skipReason && <p className="mt-1 text-xs text-slate-500">{notification.skipReason}</p>}
                                        </td>
                                        <td className="px-5 py-3 text-slate-300">{dateTime(notification.createdAt)}</td>
                                        <td className="px-5 py-3 text-right text-slate-300">{deliveryTime(notification) ?? "-"}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
                <Pagination page={data} onChange={setPage} />
            </div>
        </div>
    );
}
