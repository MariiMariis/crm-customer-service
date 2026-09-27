"use client";

import Link from "next/link";
import { useState } from "react";
import { Building2, Plus } from "lucide-react";
import { api } from "@/lib/api";
import { useApi, useDebounced } from "@/lib/useApi";
import { COMPANY_SIZES, COMPANY_TYPES, INDUSTRIES, STATES, labelOf } from "@/lib/labels";
import { moneyCompact } from "@/lib/format";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import Button from "@/components/ui/Button";
import { LabeledBadge } from "@/components/ui/Badge";
import { Select } from "@/components/ui/Field";
import { EmptyState, ErrorBanner, Spinner } from "@/components/ui/Feedback";
import { PageHeader, Pagination } from "@/components/ui/Layout";
import SearchInput from "@/components/shared/SearchInput";
import CompanyForm from "@/components/companies/CompanyForm";

export default function CompaniesPage() {
    const { activeSalesReps } = useCurrentUser();
    const [term, setTerm] = useState("");
    const [filters, setFilters] = useState({ type: "", industry: "", state: "", ownerId: "", includeArchived: false });
    const [page, setPage] = useState(0);
    const [creating, setCreating] = useState(false);
    const q = useDebounced(term);
    const { data, loading, error, reload } = useApi(
        () => api.companies.list({ q, ...filters, page, pageSize: 15 }),
        [q, filters, page]
    );
    const setFilter = (field) => (event) => {
        setPage(0);
        setFilters((current) => ({ ...current, [field]: event.target.value }));
    };

    return (
        <>
            <PageHeader
                title="Empresas"
                subtitle="Carteira de contas: prospects, clientes e parceiros."
                actions={<Button icon={Plus} onClick={() => setCreating(true)}>Nova empresa</Button>}
            />
            <div className="card">
                <div className="flex flex-col gap-3 border-b border-surface-700/70 p-4 lg:flex-row lg:items-end">
                    <SearchInput value={term} onChange={(value) => { setTerm(value); setPage(0); }} placeholder="Razão social, nome fantasia ou CNPJ" />
                    <div className="grid flex-1 grid-cols-2 gap-3 md:grid-cols-4">
                        <Select options={COMPANY_TYPES} placeholder="Todos os tipos" value={filters.type} onChange={setFilter("type")} />
                        <Select options={INDUSTRIES} placeholder="Todos os segmentos" value={filters.industry} onChange={setFilter("industry")} />
                        <Select options={STATES} placeholder="Todas as UFs" value={filters.state} onChange={setFilter("state")} />
                        <Select
                            options={activeSalesReps.map((rep) => ({ value: rep.id, label: rep.name }))}
                            placeholder="Todos os vendedores"
                            value={filters.ownerId}
                            onChange={setFilter("ownerId")}
                        />
                    </div>
                </div>
                <ErrorBanner error={error} onRetry={reload} />
                {loading && !data ? (
                    <Spinner />
                ) : data?.content.length === 0 ? (
                    <EmptyState icon={Building2} title="Nenhuma empresa encontrada" description="Ajuste os filtros ou cadastre uma nova conta." />
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead>
                                <tr className="table-head">
                                    <th className="px-5 py-3">Empresa</th>
                                    <th className="px-5 py-3">Segmento</th>
                                    <th className="px-5 py-3">Porte</th>
                                    <th className="px-5 py-3">Local</th>
                                    <th className="px-5 py-3">Faturamento</th>
                                    <th className="px-5 py-3">Responsável</th>
                                    <th className="px-5 py-3">Tipo</th>
                                </tr>
                            </thead>
                            <tbody>
                                {data?.content.map((company) => (
                                    <tr key={company.id} className="table-row">
                                        <td className="px-5 py-3">
                                            <Link href={`/companies/${company.id}`} className="font-medium text-white hover:text-brand-300">
                                                {company.displayName}
                                            </Link>
                                            <p className="text-xs text-slate-500">{company.cnpj}</p>
                                        </td>
                                        <td className="px-5 py-3 text-slate-300">{labelOf(INDUSTRIES, company.industry)}</td>
                                        <td className="px-5 py-3 text-slate-300">{labelOf(COMPANY_SIZES, company.size)}</td>
                                        <td className="px-5 py-3 text-slate-300">{[company.city, company.state].filter(Boolean).join(" / ") || "-"}</td>
                                        <td className="px-5 py-3 text-slate-300">{company.annualRevenue ? moneyCompact(company.annualRevenue) : "-"}</td>
                                        <td className="px-5 py-3 text-slate-300">{company.ownerName ?? <span className="text-slate-500">Sem dono</span>}</td>
                                        <td className="px-5 py-3"><LabeledBadge list={COMPANY_TYPES} value={company.type} /></td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
                <Pagination page={data} onChange={setPage} />
            </div>
            {creating && <CompanyForm onSaved={reload} onClose={() => setCreating(false)} />}
        </>
    );
}
