"use client";

import Link from "next/link";
import { use, useState } from "react";
import { Archive, ArchiveRestore, ArrowLeft, Globe, Pencil, Phone, Plus, Star, Users } from "lucide-react";
import { api } from "@/lib/api";
import { useApi } from "@/lib/useApi";
import { COMPANY_SIZES, COMPANY_TYPES, DECISION_ROLES, INDUSTRIES, OPPORTUNITY_STAGES, labelOf } from "@/lib/labels";
import { date, money, moneyCompact, number } from "@/lib/format";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import { useToast } from "@/components/providers/ToastProvider";
import Button from "@/components/ui/Button";
import Badge, { LabeledBadge } from "@/components/ui/Badge";
import { EmptyState, ErrorBanner, Spinner } from "@/components/ui/Feedback";
import { Avatar, DetailList, PageHeader, Section, Tabs } from "@/components/ui/Layout";
import CompanyForm from "@/components/companies/CompanyForm";
import ContactForm from "@/components/contacts/ContactForm";
import ActivityPanel from "@/components/shared/ActivityPanel";
import HistoryTimeline from "@/components/shared/HistoryTimeline";

function ContactsTab({ company }) {
    const toast = useToast();
    const [editing, setEditing] = useState(null);
    const { data, loading, error, reload } = useApi(
        () => api.contacts.list({ companyId: company.id, pageSize: 50, includeArchived: true }),
        [company.id]
    );

    const makePrimary = async (contact) => {
        try {
            await api.contacts.makePrimary(contact.id);
            toast.success(`${contact.fullName} agora é o contato principal`);
            reload();
        } catch (err) {
            toast.error(err);
        }
    };

    return (
        <div>
            {!company.archived && (
                <div className="mb-4 flex justify-end">
                    <Button size="sm" icon={Plus} onClick={() => setEditing({})}>Novo contato</Button>
                </div>
            )}
            {loading && <Spinner />}
            <ErrorBanner error={error} onRetry={reload} />
            {data?.content.length === 0 && <EmptyState icon={Users} title="Nenhum contato" description="Cadastre as pessoas envolvidas na decisão de compra." />}
            <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                {data?.content.map((contact) => (
                    <div key={contact.id} className={`rounded-lg border border-surface-700 bg-surface-850 p-4 ${contact.archived || !contact.active ? "opacity-60" : ""}`}>
                        <div className="flex items-start gap-3">
                            <Avatar name={contact.fullName} />
                            <div className="min-w-0 flex-1">
                                <div className="flex flex-wrap items-center gap-2">
                                    <p className="font-medium text-white">{contact.fullName}</p>
                                    {contact.primary && <Badge color="brand"><Star className="h-3 w-3" /> Principal</Badge>}
                                    {contact.archived && <Badge>Arquivado</Badge>}
                                    {!contact.active && !contact.archived && <Badge>Saiu da empresa</Badge>}
                                </div>
                                <p className="text-sm text-slate-400">{contact.jobTitle ?? "-"}{contact.department && ` · ${contact.department}`}</p>
                                <p className="mt-1 text-sm text-slate-300">{contact.email}</p>
                                <p className="text-xs text-slate-500">{contact.phone ?? contact.mobile ?? ""}</p>
                                <div className="mt-2"><LabeledBadge list={DECISION_ROLES} value={contact.decisionRole} /></div>
                            </div>
                        </div>
                        {!contact.archived && (
                            <div className="mt-3 flex gap-2 border-t border-surface-700 pt-3">
                                <Button size="sm" variant="ghost" icon={Pencil} onClick={() => setEditing(contact)}>Editar</Button>
                                {!contact.primary && contact.active && (
                                    <Button size="sm" variant="ghost" icon={Star} onClick={() => makePrimary(contact)}>Tornar principal</Button>
                                )}
                            </div>
                        )}
                    </div>
                ))}
            </div>
            {editing && (
                <ContactForm
                    companyId={company.id}
                    contact={editing.id ? editing : null}
                    onSaved={reload}
                    onClose={() => setEditing(null)}
                />
            )}
        </div>
    );
}

function OpportunitiesTab({ company }) {
    const { data, loading, error, reload } = useApi(
        () => api.opportunities.list({ companyId: company.id, pageSize: 50 }),
        [company.id]
    );
    if (loading) return <Spinner />;
    return (
        <>
            <ErrorBanner error={error} onRetry={reload} />
            {data?.content.length === 0 ? (
                <EmptyState title="Nenhuma oportunidade" description="As oportunidades desta empresa aparecem aqui." />
            ) : (
                <table className="w-full text-sm">
                    <thead>
                        <tr className="table-head">
                            <th className="px-3 py-2">Oportunidade</th>
                            <th className="px-3 py-2">Etapa</th>
                            <th className="px-3 py-2">Valor</th>
                            <th className="px-3 py-2">Fechamento</th>
                            <th className="px-3 py-2">Responsável</th>
                        </tr>
                    </thead>
                    <tbody>
                        {data?.content.map((opportunity) => (
                            <tr key={opportunity.id} className="table-row">
                                <td className="px-3 py-2">
                                    <Link href={`/opportunities/${opportunity.id}`} className="font-medium text-white hover:text-brand-300">{opportunity.title}</Link>
                                </td>
                                <td className="px-3 py-2"><LabeledBadge list={OPPORTUNITY_STAGES} value={opportunity.stage} /></td>
                                <td className="px-3 py-2 text-slate-300">{money(opportunity.amount)}</td>
                                <td className="px-3 py-2 text-slate-300">{date(opportunity.expectedCloseDate)}</td>
                                <td className="px-3 py-2 text-slate-300">{opportunity.ownerName}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </>
    );
}

export default function CompanyDetailPage({ params }) {
    const { id } = use(params);
    const toast = useToast();
    const { nameOf } = useCurrentUser();
    const [tab, setTab] = useState("contacts");
    const [editing, setEditing] = useState(false);
    const { data: company, loading, error, reload } = useApi(() => api.companies.get(id), [id]);

    const toggleArchive = async () => {
        try {
            await (company.archived ? api.companies.restore(id) : api.companies.archive(id));
            toast.success(company.archived ? "Empresa restaurada" : "Empresa arquivada com os seus contatos");
            reload();
        } catch (err) {
            toast.error(err);
        }
    };

    if (loading && !company) return <Spinner />;
    if (!company) return <ErrorBanner error={error} onRetry={reload} />;

    return (
        <>
            <Link href="/companies" className="mb-4 inline-flex items-center gap-1 text-sm text-slate-400 hover:text-white">
                <ArrowLeft className="h-4 w-4" /> Empresas
            </Link>
            <PageHeader
                title={company.displayName}
                subtitle={`${company.legalName} · CNPJ ${company.cnpj}`}
                actions={
                    <>
                        {!company.archived && <Button variant="secondary" icon={Pencil} onClick={() => setEditing(true)}>Editar</Button>}
                        <Button variant={company.archived ? "secondary" : "danger"} icon={company.archived ? ArchiveRestore : Archive} onClick={toggleArchive}>
                            {company.archived ? "Restaurar" : "Arquivar"}
                        </Button>
                    </>
                }
            >
                <div className="mt-3 flex flex-wrap gap-2">
                    <LabeledBadge list={COMPANY_TYPES} value={company.type} />
                    <Badge>{labelOf(INDUSTRIES, company.industry)}</Badge>
                    <Badge>{labelOf(COMPANY_SIZES, company.size)}</Badge>
                    {company.archived && <Badge color="rose">Arquivada</Badge>}
                </div>
            </PageHeader>

            <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
                <Section title="Dados da conta" className="xl:col-span-1">
                    <DetailList
                        items={[
                            { label: "Responsável", value: company.ownerName ?? "Sem responsável" },
                            { label: "Contato principal", value: company.primaryContactName },
                            { label: "Contatos ativos", value: number(company.contactCount) },
                            { label: "Funcionários", value: company.employees ? number(company.employees) : null },
                            { label: "Faturamento anual", value: company.annualRevenue ? moneyCompact(company.annualRevenue) : null },
                            { label: "Localização", value: [company.city, company.state].filter(Boolean).join(" / ") },
                            {
                                label: "Site",
                                value: company.website ? (
                                    <a href={company.website} target="_blank" rel="noreferrer" className="inline-flex items-center gap-1 text-brand-300 hover:underline">
                                        <Globe className="h-3.5 w-3.5" /> {company.website.replace(/^https?:\/\//, "")}
                                    </a>
                                ) : null,
                            },
                            { label: "Telefone", value: company.phone ? <span className="inline-flex items-center gap-1"><Phone className="h-3.5 w-3.5" /> {company.phone}</span> : null },
                        ]}
                    />
                    {company.notes && <p className="mt-5 rounded-lg bg-surface-850 p-3 text-sm text-slate-300">{company.notes}</p>}
                </Section>
                <div className="xl:col-span-2">
                    <Tabs
                        active={tab}
                        onChange={setTab}
                        tabs={[
                            { value: "contacts", label: "Contatos", count: company.contactCount },
                            { value: "opportunities", label: "Oportunidades" },
                            { value: "activities", label: "Atividades" },
                            { value: "history", label: "Histórico" },
                        ]}
                    />
                    <div className="card p-5">
                        {tab === "contacts" && <ContactsTab company={company} />}
                        {tab === "opportunities" && <OpportunitiesTab company={company} />}
                        {tab === "activities" && <ActivityPanel relatedType="COMPANY" relatedId={company.id} />}
                        {tab === "history" && (
                            <HistoryTimeline
                                loader={() => api.companies.revisions(company.id)}
                                describe={(data) => `${labelOf(COMPANY_TYPES, data.type)} · responsável ${nameOf(data.ownerId) ?? "-"}${data.archived ? " · arquivada" : ""}`}
                            />
                        )}
                    </div>
                </div>
            </div>
            {editing && <CompanyForm company={company} onSaved={reload} onClose={() => setEditing(false)} />}
        </>
    );
}
