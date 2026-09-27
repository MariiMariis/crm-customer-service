"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import { Bar, BarChart, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { AlarmClock, CalendarCheck, DollarSign, ShieldAlert, Target, TrendingUp } from "lucide-react";
import { api } from "@/lib/api";
import { useApi } from "@/lib/useApi";
import { ACTIVITY_TYPES, LEAD_STATUSES, OPEN_STAGES, OPPORTUNITY_STAGES, labelOf } from "@/lib/labels";
import { addDays, date, dateTime, money, moneyCompact } from "@/lib/format";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import Badge, { LabeledBadge } from "@/components/ui/Badge";
import { EmptyState, ErrorBanner, Spinner } from "@/components/ui/Feedback";
import { KpiCard, PageHeader, Section } from "@/components/ui/Layout";

const STAGE_COLORS = { PROSPECTING: "#0ea5e9", QUALIFICATION: "#06b6d4", PROPOSAL: "#8b5cf6", NEGOTIATION: "#f59e0b" };
const LEAD_COLORS = { NEW: "#0ea5e9", CONTACTED: "#06b6d4", QUALIFIED: "#8b5cf6", CONVERTING: "#f59e0b", CONVERTED: "#10b981", UNQUALIFIED: "#f43f5e" };

function ChartTooltip({ active, payload, formatter }) {
    if (!active || !payload?.length) return null;
    const item = payload[0];
    return (
        <div className="rounded-lg border border-surface-700 bg-surface-850 px-3 py-2 text-xs shadow-xl">
            <p className="font-medium text-white">{item.payload.label}</p>
            <p className="text-slate-300">{formatter(item.value, item.payload)}</p>
        </div>
    );
}

function QuotaProgress({ won, quota }) {
    const ratio = quota > 0 ? Math.min(won / quota, 1.5) : 0;
    const percentValue = quota > 0 ? Math.round((won / quota) * 100) : 0;
    return (
        <div className="card p-5">
            <p className="text-xs font-medium uppercase tracking-wide text-slate-400">Meta do mês × realizado</p>
            <p className="mt-2 text-2xl font-semibold text-white">
                {moneyCompact(won)} <span className="text-base font-normal text-slate-400">de {moneyCompact(quota)}</span>
            </p>
            <div className="mt-3 h-2.5 overflow-hidden rounded-full bg-surface-800">
                <div
                    className={`h-full rounded-full ${percentValue >= 100 ? "bg-emerald-500" : "bg-gradient-to-r from-brand-600 to-brand-400"}`}
                    style={{ width: `${Math.min(ratio, 1) * 100}%` }}
                />
            </div>
            <p className="mt-2 text-xs text-slate-400">{quota > 0 ? `${percentValue}% da meta atingida` : "Sem meta definida para esta visão"}</p>
        </div>
    );
}

export default function DashboardPage() {
    const { user, salesReps, isManager, subordinates, error: userError } = useCurrentUser();
    const [scope, setScope] = useState(null);
    const effectiveScope = scope ?? (isManager ? "team" : "mine");
    const ownerId = effectiveScope === "mine" ? user?.id : undefined;
    const ready = Boolean(user);

    const { data: pipeline, loading, error } = useApi(() => (ready ? api.opportunities.pipeline(ownerId) : null), [ready, ownerId]);
    const { data: won } = useApi(() => (ready ? api.opportunities.list({ stage: "WON", ownerId, pageSize: 100 }) : null), [ready, ownerId]);
    const { data: leadStats } = useApi(() => api.leads.stats(), []);
    const { data: activitySummary } = useApi(() => (ready ? api.activities.summary(ownerId) : null), [ready, ownerId]);
    const { data: upcoming } = useApi(
        () => (ready ? api.activities.list({ ownerId, status: "PLANNED", dueTo: `${addDays(7)}T23:59:59Z`, pageSize: 6 }) : null),
        [ready, ownerId]
    );
    const { data: closing } = useApi(
        () => (ready ? api.opportunities.list({ open: true, ownerId, closingTo: addDays(30), pageSize: 6 }) : null),
        [ready, ownerId]
    );

    const wonThisMonth = useMemo(() => {
        const now = new Date();
        return (won?.content ?? []).filter((opportunity) => {
            const closed = new Date(opportunity.closedAt);
            return closed.getMonth() === now.getMonth() && closed.getFullYear() === now.getFullYear();
        });
    }, [won]);
    const wonAmount = wonThisMonth.reduce((sum, opportunity) => sum + Number(opportunity.amount), 0);
    const wonMrr = wonThisMonth.reduce((sum, opportunity) => sum + Number(opportunity.monthlyRecurringValue), 0);

    const quota = useMemo(() => {
        if (effectiveScope === "mine") return Number(user?.monthlyQuota ?? 0);
        const team = isManager && subordinates.length ? subordinates : salesReps.filter((rep) => rep.role === "REP" && rep.active);
        return team.reduce((sum, rep) => sum + Number(rep.monthlyQuota ?? 0), 0);
    }, [effectiveScope, user, isManager, subordinates, salesReps]);

    const stageData = (pipeline?.stages ?? [])
        .filter((stage) => OPEN_STAGES.some((open) => open.value === stage.stage))
        .map((stage) => ({ ...stage, label: labelOf(OPPORTUNITY_STAGES, stage.stage), amount: Number(stage.amount) }));
    const leadData = LEAD_STATUSES.map((status) => ({ key: status.value, label: status.label, value: leadStats?.byStatus?.[status.value] ?? 0 })).filter(
        (entry) => entry.value > 0
    );

    if (userError) return <ErrorBanner error={userError} />;
    if (!ready) return <Spinner label="Carregando sua carteira..." />;

    return (
        <>
            <PageHeader
                title={`Olá, ${user.name.split(" ")[0]}`}
                subtitle={effectiveScope === "mine" ? "Sua carteira comercial" : isManager ? "Visão da sua equipe" : "Visão de toda a equipe comercial"}
                actions={
                    <div className="flex rounded-lg bg-surface-850 p-1 ring-1 ring-surface-700">
                        {[
                            { value: "mine", label: "Minha carteira" },
                            { value: "team", label: isManager ? "Minha equipe" : "Toda a equipe" },
                        ].map((option) => (
                            <button
                                key={option.value}
                                onClick={() => setScope(option.value)}
                                className={`rounded-md px-3 py-1.5 text-sm transition ${effectiveScope === option.value ? "bg-brand-500 text-white" : "text-slate-400 hover:text-white"}`}
                            >
                                {option.label}
                            </button>
                        ))}
                    </div>
                }
            />
            <ErrorBanner error={error} />
            {loading && !pipeline ? (
                <Spinner />
            ) : (
                <>
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
                        <KpiCard label="Pipeline aberto" value={moneyCompact(pipeline?.openAmount)} hint={`${pipeline?.openCount ?? 0} oportunidades`} icon={TrendingUp} />
                        <KpiCard label="Forecast ponderado" value={moneyCompact(pipeline?.weightedForecast)} hint="Valor × probabilidade" icon={Target} tone="violet" />
                        <KpiCard label="Ganho no mês" value={moneyCompact(wonAmount)} hint={`${wonThisMonth.length} negócio(s) · MRR ${moneyCompact(wonMrr)}`} icon={DollarSign} tone="emerald" />
                        <KpiCard
                            label="Aprovações de desconto"
                            value={pipeline?.pendingDiscountApprovals ?? 0}
                            hint={isManager ? "Aguardando sua decisão ou da gestão" : "Aguardando o gestor"}
                            icon={ShieldAlert}
                            tone="amber"
                        />
                    </div>

                    <div className="mt-6 grid grid-cols-1 gap-6 xl:grid-cols-3">
                        <Section title="Pipeline por etapa" className="xl:col-span-2" actions={<Link href="/pipeline" className="text-xs text-brand-300 hover:underline">Abrir Kanban</Link>}>
                            <div className="h-64">
                                <ResponsiveContainer width="100%" height="100%">
                                    <BarChart data={stageData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                                        <XAxis dataKey="label" stroke="#64748b" fontSize={12} tickLine={false} axisLine={false} />
                                        <YAxis stroke="#64748b" fontSize={11} tickLine={false} axisLine={false} tickFormatter={(value) => moneyCompact(value)} width={80} />
                                        <Tooltip cursor={{ fill: "rgba(14,165,233,0.08)" }} content={<ChartTooltip formatter={(value, row) => `${money(value)} · ${row.count} oportunidade(s)`} />} />
                                        <Bar dataKey="amount" radius={[6, 6, 0, 0]}>
                                            {stageData.map((entry) => (
                                                <Cell key={entry.stage} fill={STAGE_COLORS[entry.stage]} />
                                            ))}
                                        </Bar>
                                    </BarChart>
                                </ResponsiveContainer>
                            </div>
                        </Section>
                        <QuotaProgress won={wonAmount} quota={quota} />
                    </div>

                    <div className="mt-6 grid grid-cols-1 gap-6 xl:grid-cols-3">
                        <Section title="Leads por status" actions={<Link href="/leads" className="text-xs text-brand-300 hover:underline">Ver leads</Link>}>
                            <div className="h-48">
                                <ResponsiveContainer width="100%" height="100%">
                                    <PieChart>
                                        <Pie data={leadData} dataKey="value" nameKey="label" innerRadius={48} outerRadius={78} paddingAngle={2} stroke="none">
                                            {leadData.map((entry) => (
                                                <Cell key={entry.key} fill={LEAD_COLORS[entry.key]} />
                                            ))}
                                        </Pie>
                                        <Tooltip content={<ChartTooltip formatter={(value) => `${value} lead(s)`} />} />
                                    </PieChart>
                                </ResponsiveContainer>
                            </div>
                            <ul className="mt-2 grid grid-cols-2 gap-1 text-xs text-slate-300">
                                {leadData.map((entry) => (
                                    <li key={entry.key} className="flex items-center gap-2">
                                        <span className="h-2 w-2 rounded-full" style={{ background: LEAD_COLORS[entry.key] }} />
                                        {entry.label}: {entry.value}
                                    </li>
                                ))}
                            </ul>
                        </Section>

                        <Section title="Agenda" actions={<span className="text-xs text-slate-400">{activitySummary?.overdue ?? 0} atrasada(s) · {activitySummary?.dueToday ?? 0} hoje</span>}>
                            {upcoming?.content.length === 0 ? (
                                <EmptyState icon={CalendarCheck} title="Agenda livre" description="Nenhuma atividade planejada para os próximos 7 dias." />
                            ) : (
                                <ul className="space-y-2">
                                    {upcoming?.content.map((activity) => (
                                        <li key={activity.id} className="flex items-start justify-between gap-3 rounded-lg bg-surface-850 px-3 py-2">
                                            <div className="min-w-0">
                                                <p className="truncate text-sm text-white">{activity.subject}</p>
                                                <p className="truncate text-xs text-slate-400">
                                                    {labelOf(ACTIVITY_TYPES, activity.type)} · {activity.relatedName}
                                                </p>
                                            </div>
                                            <span className={`shrink-0 text-xs ${activity.overdue ? "text-rose-300" : "text-slate-400"}`}>
                                                {activity.overdue && <AlarmClock className="mr-1 inline h-3 w-3" />}
                                                {dateTime(activity.startsAt ?? activity.dueAt)}
                                            </span>
                                        </li>
                                    ))}
                                </ul>
                            )}
                        </Section>

                        <Section title="Fechando em 30 dias">
                            {closing?.content.length === 0 ? (
                                <EmptyState title="Nada previsto" description="Nenhuma oportunidade aberta com fechamento nos próximos 30 dias." />
                            ) : (
                                <ul className="space-y-2">
                                    {closing?.content.map((opportunity) => (
                                        <li key={opportunity.id}>
                                            <Link href={`/opportunities/${opportunity.id}`} className="flex items-center justify-between gap-3 rounded-lg bg-surface-850 px-3 py-2 hover:bg-surface-800">
                                                <div className="min-w-0">
                                                    <p className="truncate text-sm text-white">{opportunity.title}</p>
                                                    <p className="truncate text-xs text-slate-400">{opportunity.companyName} · {date(opportunity.expectedCloseDate)}</p>
                                                </div>
                                                <div className="shrink-0 text-right">
                                                    <p className="text-sm font-medium text-white">{moneyCompact(opportunity.amount)}</p>
                                                    <LabeledBadge list={OPPORTUNITY_STAGES} value={opportunity.stage} />
                                                </div>
                                            </Link>
                                        </li>
                                    ))}
                                </ul>
                            )}
                            {pipeline?.monthlyRecurringWon > 0 && (
                                <p className="mt-4 text-xs text-slate-400">
                                    <Badge color="emerald">MRR contratado</Badge> {money(pipeline.monthlyRecurringWon)} por mês em contratos ganhos.
                                </p>
                            )}
                        </Section>
                    </div>
                </>
            )}
        </>
    );
}
