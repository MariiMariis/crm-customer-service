"use client";

import Link from "next/link";
import { useState } from "react";
import { Briefcase, Plus } from "lucide-react";
import { api } from "@/lib/api";
import { useApi, useDebounced } from "@/lib/useApi";
import { DISCOUNT_APPROVAL, OPPORTUNITY_STAGES } from "@/lib/labels";
import { date, money, percent } from "@/lib/format";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import Button from "@/components/ui/Button";
import Badge, { LabeledBadge } from "@/components/ui/Badge";
import { Select } from "@/components/ui/Field";
import { EmptyState, ErrorBanner, Spinner } from "@/components/ui/Feedback";
import { PageHeader, Pagination } from "@/components/ui/Layout";
import SearchInput from "@/components/shared/SearchInput";
import OpportunityForm from "@/components/opportunities/OpportunityForm";

export default function OpportunitiesPage() {
    const { activeSalesReps } = useCurrentUser();
    const [term, setTerm] = useState("");
    const [filters, setFilters] = useState({ stage: "", open: "", ownerId: "", discountApproval: "" });
    const [page, setPage] = useState(0);
    const [creating, setCreating] = useState(false);
    const q = useDebounced(term);
    const { data, loading, error, reload } = useApi(
        () => api.opportunities.list({ q, ...filters, page, pageSize: 15 }),
        [q, filters, page]
    );
    const setFilter = (field) => (event) => {
        setPage(0);
        setFilters((current) => ({ ...current, [field]: event.target.value }));
    };

    return (
        <>
            <PageHeader
                title="Oportunidades"
                subtitle="Negócios em andamento com valor único, recorrência (MRR) e forecast ponderado."
                actions={
                    <>
                        <Button variant="secondary" href="/pipeline">Ver pipeline</Button>
                        <Button icon={Plus} onClick={() => setCreating(true)}>Nova oportunidade</Button>
                    </>
                }
            />
            <div className="card">
                <div className="flex flex-col gap-3 border-b border-surface-700/70 p-4 lg:flex-row">
                    <SearchInput value={term} onChange={(value) => { setTerm(value); setPage(0); }} placeholder="Título da oportunidade" />
                    <div className="grid flex-1 grid-cols-2 gap-3 md:grid-cols-4">
                        <Select options={OPPORTUNITY_STAGES} placeholder="Todas as etapas" value={filters.stage} onChange={setFilter("stage")} />
                        <Select
                            options={[{ value: "true", label: "Abertas" }, { value: "false", label: "Encerradas" }]}
                            placeholder="Abertas e encerradas"
                            value={filters.open}
                            onChange={setFilter("open")}
                        />
                        <Select options={activeSalesReps.map((rep) => ({ value: rep.id, label: rep.name }))} placeholder="Todos os responsáveis" value={filters.ownerId} onChange={setFilter("ownerId")} />
                        <Select options={DISCOUNT_APPROVAL} placeholder="Qualquer desconto" value={filters.discountApproval} onChange={setFilter("discountApproval")} />
                    </div>
                </div>
                <ErrorBanner error={error} onRetry={reload} />
                {loading && !data ? (
                    <Spinner />
                ) : data?.content.length === 0 ? (
                    <EmptyState icon={Briefcase} title="Nenhuma oportunidade encontrada" />
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead>
                                <tr className="table-head">
                                    <th className="px-5 py-3">Oportunidade</th>
                                    <th className="px-5 py-3">Etapa</th>
                                    <th className="px-5 py-3 text-right">Valor total</th>
                                    <th className="px-5 py-3 text-right">MRR</th>
                                    <th className="px-5 py-3 text-right">Prob.</th>
                                    <th className="px-5 py-3">Fechamento</th>
                                    <th className="px-5 py-3">Responsável</th>
                                </tr>
                            </thead>
                            <tbody>
                                {data?.content.map((opportunity) => (
                                    <tr key={opportunity.id} className="table-row">
                                        <td className="px-5 py-3">
                                            <Link href={`/opportunities/${opportunity.id}`} className="font-medium text-white hover:text-brand-300">{opportunity.title}</Link>
                                            <p className="text-xs text-slate-500">{opportunity.companyName}</p>
                                        </td>
                                        <td className="px-5 py-3">
                                            <div className="flex flex-wrap gap-1">
                                                <LabeledBadge list={OPPORTUNITY_STAGES} value={opportunity.stage} />
                                                {opportunity.discountApproval === "PENDING" && <Badge color="amber">Desconto pendente</Badge>}
                                            </div>
                                        </td>
                                        <td className="px-5 py-3 text-right font-medium text-white">{money(opportunity.amount)}</td>
                                        <td className="px-5 py-3 text-right text-slate-300">{Number(opportunity.monthlyRecurringValue) > 0 ? money(opportunity.monthlyRecurringValue) : "-"}</td>
                                        <td className="px-5 py-3 text-right text-slate-300">{percent(opportunity.probability)}</td>
                                        <td className="px-5 py-3 text-slate-300">{date(opportunity.expectedCloseDate)}</td>
                                        <td className="px-5 py-3 text-slate-300">{opportunity.ownerName}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
                <Pagination page={data} onChange={setPage} />
            </div>
            {creating && <OpportunityForm onSaved={reload} onClose={() => setCreating(false)} />}
        </>
    );
}
