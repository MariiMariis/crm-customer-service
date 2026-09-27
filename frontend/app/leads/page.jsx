"use client";

import Link from "next/link";
import { useState } from "react";
import { Plus, Target } from "lucide-react";
import { api } from "@/lib/api";
import { useApi, useDebounced } from "@/lib/useApi";
import { LEAD_SOURCES, LEAD_STATUSES, labelOf } from "@/lib/labels";
import { moneyCompact, timeAgo } from "@/lib/format";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import Button from "@/components/ui/Button";
import { LabeledBadge } from "@/components/ui/Badge";
import { Select } from "@/components/ui/Field";
import { EmptyState, ErrorBanner, Spinner } from "@/components/ui/Feedback";
import { PageHeader, Pagination } from "@/components/ui/Layout";
import SearchInput from "@/components/shared/SearchInput";
import LeadForm from "@/components/leads/LeadForm";
import ScoreRing from "@/components/leads/ScoreRing";

export default function LeadsPage() {
    const { activeSalesReps } = useCurrentUser();
    const [term, setTerm] = useState("");
    const [filters, setFilters] = useState({ status: "", source: "", owner: "", minScore: "" });
    const [page, setPage] = useState(0);
    const [creating, setCreating] = useState(false);
    const q = useDebounced(term);
    const { data: stats, reload: reloadStats } = useApi(() => api.leads.stats(), []);
    const { data, loading, error, reload } = useApi(
        () =>
            api.leads.list({
                q,
                status: filters.status,
                source: filters.source,
                ownerId: filters.owner === "none" ? "" : filters.owner,
                unassigned: filters.owner === "none" ? true : "",
                minScore: filters.minScore,
                page,
                pageSize: 15,
            }),
        [q, filters, page]
    );
    const setFilter = (field, value) => {
        setPage(0);
        setFilters((current) => ({ ...current, [field]: current[field] === value && field === "status" ? "" : value }));
    };

    return (
        <>
            <PageHeader
                title="Leads"
                subtitle="Prospects ordenados pelo score. Qualifique e converta em empresa, contato e oportunidade."
                actions={<Button icon={Plus} onClick={() => setCreating(true)}>Novo lead</Button>}
            />
            <div className="mb-5 grid grid-cols-2 gap-3 md:grid-cols-6">
                {LEAD_STATUSES.map((status) => (
                    <button
                        key={status.value}
                        onClick={() => setFilter("status", status.value)}
                        className={`card px-4 py-3 text-left transition hover:border-brand-500/50 ${filters.status === status.value ? "border-brand-500 ring-1 ring-brand-500/40" : ""}`}
                    >
                        <p className="text-xs text-slate-400">{status.label}</p>
                        <p className="mt-1 text-xl font-semibold text-white">{stats?.byStatus?.[status.value] ?? 0}</p>
                    </button>
                ))}
            </div>
            <div className="card">
                <div className="flex flex-col gap-3 border-b border-surface-700/70 p-4 lg:flex-row">
                    <SearchInput value={term} onChange={(value) => { setTerm(value); setPage(0); }} placeholder="Nome, e-mail ou empresa" />
                    <div className="grid flex-1 grid-cols-1 gap-3 sm:grid-cols-3">
                        <Select options={LEAD_SOURCES} placeholder="Todas as origens" value={filters.source} onChange={(event) => setFilter("source", event.target.value)} />
                        <Select
                            options={[{ value: "none", label: "Fila (sem responsável)" }, ...activeSalesReps.map((rep) => ({ value: rep.id, label: rep.name }))]}
                            placeholder="Todos os responsáveis"
                            value={filters.owner}
                            onChange={(event) => setFilter("owner", event.target.value)}
                        />
                        <Select
                            options={[{ value: "70", label: "Score 70+ (quente)" }, { value: "40", label: "Score 40+ (morno)" }]}
                            placeholder="Qualquer score"
                            value={filters.minScore}
                            onChange={(event) => setFilter("minScore", event.target.value)}
                        />
                    </div>
                </div>
                <ErrorBanner error={error} onRetry={reload} />
                {loading && !data ? (
                    <Spinner />
                ) : data?.content.length === 0 ? (
                    <EmptyState icon={Target} title="Nenhum lead encontrado" />
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead>
                                <tr className="table-head">
                                    <th className="px-5 py-3">Score</th>
                                    <th className="px-5 py-3">Lead</th>
                                    <th className="px-5 py-3">Empresa</th>
                                    <th className="px-5 py-3">Origem</th>
                                    <th className="px-5 py-3">Valor estimado</th>
                                    <th className="px-5 py-3">Responsável</th>
                                    <th className="px-5 py-3">Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {data?.content.map((lead) => (
                                    <tr key={lead.id} className="table-row">
                                        <td className="px-5 py-2"><ScoreRing score={lead.score} size={38} /></td>
                                        <td className="px-5 py-3">
                                            <Link href={`/leads/${lead.id}`} className="font-medium text-white hover:text-brand-300">{lead.fullName}</Link>
                                            <p className="text-xs text-slate-500">{lead.jobTitle ?? lead.email ?? lead.phone} · criado {timeAgo(lead.createdAt)}</p>
                                        </td>
                                        <td className="px-5 py-3 text-slate-300">{lead.companyName}</td>
                                        <td className="px-5 py-3 text-slate-300">{labelOf(LEAD_SOURCES, lead.source)}</td>
                                        <td className="px-5 py-3 text-slate-300">{lead.estimatedValue ? moneyCompact(lead.estimatedValue) : "-"}</td>
                                        <td className="px-5 py-3 text-slate-300">{lead.ownerName ?? <span className="text-amber-300/80">Na fila</span>}</td>
                                        <td className="px-5 py-3"><LabeledBadge list={LEAD_STATUSES} value={lead.status} /></td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
                <Pagination page={data} onChange={setPage} />
            </div>
            {creating && <LeadForm onSaved={() => { reload(); reloadStats(); }} onClose={() => setCreating(false)} />}
        </>
    );
}
