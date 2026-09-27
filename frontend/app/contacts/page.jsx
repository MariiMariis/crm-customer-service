"use client";

import Link from "next/link";
import { useState } from "react";
import { Plus, Star, Users } from "lucide-react";
import { api } from "@/lib/api";
import { useApi, useDebounced } from "@/lib/useApi";
import { DECISION_ROLES } from "@/lib/labels";
import Button from "@/components/ui/Button";
import Badge, { LabeledBadge } from "@/components/ui/Badge";
import { Select } from "@/components/ui/Field";
import { EmptyState, ErrorBanner, Spinner } from "@/components/ui/Feedback";
import { Avatar, PageHeader, Pagination } from "@/components/ui/Layout";
import SearchInput from "@/components/shared/SearchInput";
import ContactForm from "@/components/contacts/ContactForm";

export default function ContactsPage() {
    const [term, setTerm] = useState("");
    const [decisionRole, setDecisionRole] = useState("");
    const [page, setPage] = useState(0);
    const [editing, setEditing] = useState(null);
    const q = useDebounced(term);
    const { data, loading, error, reload } = useApi(
        () => api.contacts.list({ q, decisionRole, page, pageSize: 15 }),
        [q, decisionRole, page]
    );
    const { data: companies } = useApi(() => api.companies.list({ pageSize: 100 }), []);

    return (
        <>
            <PageHeader
                title="Contatos"
                subtitle="Pessoas das empresas-cliente e o papel de cada uma na decisão de compra."
                actions={<Button icon={Plus} onClick={() => setEditing({})}>Novo contato</Button>}
            />
            <div className="card">
                <div className="flex flex-col gap-3 border-b border-surface-700/70 p-4 sm:flex-row">
                    <SearchInput value={term} onChange={(value) => { setTerm(value); setPage(0); }} placeholder="Nome, e-mail ou cargo" />
                    <Select
                        className="sm:w-60"
                        options={DECISION_ROLES}
                        placeholder="Todos os papéis"
                        value={decisionRole}
                        onChange={(event) => { setDecisionRole(event.target.value); setPage(0); }}
                    />
                </div>
                <ErrorBanner error={error} onRetry={reload} />
                {loading && !data ? (
                    <Spinner />
                ) : data?.content.length === 0 ? (
                    <EmptyState icon={Users} title="Nenhum contato encontrado" />
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead>
                                <tr className="table-head">
                                    <th className="px-5 py-3">Contato</th>
                                    <th className="px-5 py-3">Empresa</th>
                                    <th className="px-5 py-3">Cargo</th>
                                    <th className="px-5 py-3">Telefone</th>
                                    <th className="px-5 py-3">Papel</th>
                                </tr>
                            </thead>
                            <tbody>
                                {data?.content.map((contact) => (
                                    <tr key={contact.id} className="table-row cursor-pointer" onClick={() => setEditing(contact)}>
                                        <td className="px-5 py-3">
                                            <div className="flex items-center gap-3">
                                                <Avatar name={contact.fullName} size="sm" />
                                                <div>
                                                    <p className="flex items-center gap-2 font-medium text-white">
                                                        {contact.fullName}
                                                        {contact.primary && <Star className="h-3.5 w-3.5 fill-brand-400 text-brand-400" />}
                                                    </p>
                                                    <p className="text-xs text-slate-500">{contact.email}</p>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-5 py-3">
                                            <Link href={`/companies/${contact.companyId}`} onClick={(event) => event.stopPropagation()} className="text-slate-300 hover:text-brand-300">
                                                {contact.companyName}
                                            </Link>
                                        </td>
                                        <td className="px-5 py-3 text-slate-300">{contact.jobTitle ?? "-"}</td>
                                        <td className="px-5 py-3 text-slate-300">{contact.mobile ?? contact.phone ?? "-"}</td>
                                        <td className="px-5 py-3">
                                            <LabeledBadge list={DECISION_ROLES} value={contact.decisionRole} />
                                            {!contact.active && <Badge className="ml-2">Inativo</Badge>}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
                <Pagination page={data} onChange={setPage} />
            </div>
            {editing && (
                <ContactForm
                    contact={editing.id ? editing : null}
                    companies={companies?.content ?? []}
                    onSaved={reload}
                    onClose={() => setEditing(null)}
                />
            )}
        </>
    );
}
