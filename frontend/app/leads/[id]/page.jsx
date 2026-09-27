"use client";

import Link from "next/link";
import { use, useEffect, useState } from "react";
import { ArrowLeft, ArrowRightLeft, Ban, CheckCheck, Loader2, Pencil, PhoneCall, RotateCcw, UserPlus } from "lucide-react";
import { api } from "@/lib/api";
import { useApi } from "@/lib/useApi";
import { COMPANY_SIZES, INDUSTRIES, LEAD_SOURCES, LEAD_STATUSES, STATES, labelOf } from "@/lib/labels";
import { addDays, dateTime, money } from "@/lib/format";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import { useToast } from "@/components/providers/ToastProvider";
import Button from "@/components/ui/Button";
import Badge, { LabeledBadge } from "@/components/ui/Badge";
import Modal from "@/components/ui/Modal";
import { Input, Select, Textarea } from "@/components/ui/Field";
import { ErrorBanner, Spinner } from "@/components/ui/Feedback";
import { DetailList, PageHeader, Section, Tabs } from "@/components/ui/Layout";
import ActivityPanel from "@/components/shared/ActivityPanel";
import HistoryTimeline from "@/components/shared/HistoryTimeline";
import LeadForm from "@/components/leads/LeadForm";
import ScoreRing from "@/components/leads/ScoreRing";

const FUNNEL = ["NEW", "CONTACTED", "QUALIFIED", "CONVERTED"];

function FunnelStepper({ status }) {
    const current = status === "CONVERTING" ? 2 : FUNNEL.indexOf(status);
    return (
        <ol className="flex items-center gap-2">
            {FUNNEL.map((step, index) => {
                const done = status !== "UNQUALIFIED" && index <= current;
                return (
                    <li key={step} className="flex flex-1 items-center gap-2">
                        <span className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-xs font-bold ${done ? "bg-brand-500 text-white" : "bg-surface-800 text-slate-500"}`}>
                            {index + 1}
                        </span>
                        <span className={`text-sm ${done ? "text-white" : "text-slate-500"}`}>{labelOf(LEAD_STATUSES, step)}</span>
                        {index < FUNNEL.length - 1 && <span className={`h-px flex-1 ${done && index < current ? "bg-brand-500" : "bg-surface-700"}`} />}
                    </li>
                );
            })}
        </ol>
    );
}

function ConvertModal({ lead, onConverted, onClose }) {
    const toast = useToast();
    const [saving, setSaving] = useState(false);
    const [form, setForm] = useState({
        cnpj: lead.conversion?.cnpj ?? "",
        industry: lead.conversion?.industry ?? "TECHNOLOGY",
        companySize: lead.conversion?.companySize ?? "MEDIUM",
        city: lead.conversion?.city ?? "",
        state: lead.conversion?.state ?? "SP",
        createOpportunity: true,
        opportunityTitle: `Projeto ${lead.companyName}`,
        expectedCloseDate: addDays(30),
    });
    const set = (field) => (event) => setForm((current) => ({ ...current, [field]: event.target.value }));

    const submit = async () => {
        setSaving(true);
        try {
            await api.leads.convert(lead.id, form);
            toast.success("Conversão solicitada: o serviço de contas vai criar a empresa e o contato");
            onConverted();
            onClose();
        } catch (error) {
            toast.error(error);
        } finally {
            setSaving(false);
        }
    };

    return (
        <Modal
            open
            title={`Converter ${lead.fullName}`}
            description="A conversão é assíncrona (saga): o accounts-service cria ou reaproveita a empresa pelo CNPJ e o contato, e o sales-service abre a oportunidade."
            onClose={onClose}
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>Cancelar</Button>
                    <Button icon={ArrowRightLeft} onClick={submit} loading={saving} disabled={!form.cnpj}>Converter</Button>
                </>
            }
        >
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <Input className="sm:col-span-2" label={`CNPJ de ${lead.companyName}`} value={form.cnpj} onChange={set("cnpj")} placeholder="00.000.000/0000-00" />
                <Select label="Segmento" options={INDUSTRIES} value={form.industry} onChange={set("industry")} />
                <Select label="Porte" options={COMPANY_SIZES} value={form.companySize} onChange={set("companySize")} />
                <Input label="Cidade" value={form.city} onChange={set("city")} />
                <Select label="UF" options={STATES} value={form.state} onChange={set("state")} />
                <label className="flex items-center gap-2 text-sm text-slate-300 sm:col-span-2">
                    <input
                        type="checkbox"
                        className="accent-sky-500"
                        checked={form.createOpportunity}
                        onChange={(event) => setForm((current) => ({ ...current, createOpportunity: event.target.checked }))}
                    />
                    Abrir uma oportunidade com o valor estimado do lead ({money(lead.estimatedValue ?? 0)})
                </label>
                {form.createOpportunity && (
                    <>
                        <Input label="Título da oportunidade" value={form.opportunityTitle} onChange={set("opportunityTitle")} />
                        <Input label="Fechamento previsto" type="date" value={form.expectedCloseDate} onChange={set("expectedCloseDate")} />
                    </>
                )}
            </div>
        </Modal>
    );
}

function ReasonModal({ title, label, confirm, onConfirm, onClose }) {
    const [reason, setReason] = useState("");
    return (
        <Modal
            open
            size="sm"
            title={title}
            onClose={onClose}
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>Cancelar</Button>
                    <Button variant="danger" disabled={!reason.trim()} onClick={() => onConfirm(reason)}>{confirm}</Button>
                </>
            }
        >
            <Textarea label={label} value={reason} onChange={(event) => setReason(event.target.value)} />
        </Modal>
    );
}

export default function LeadDetailPage({ params }) {
    const { id } = use(params);
    const toast = useToast();
    const { user, activeSalesReps } = useCurrentUser();
    const [tab, setTab] = useState("activities");
    const [modal, setModal] = useState(null);
    const [assignee, setAssignee] = useState("");
    const { data: lead, loading, error, reload } = useApi(() => api.leads.get(id), [id]);

    useEffect(() => {
        if (lead?.status !== "CONVERTING") return undefined;
        const timer = setInterval(reload, 1500);
        return () => clearInterval(timer);
    }, [lead?.status, reload]);

    const run = async (action, message) => {
        try {
            await action();
            toast.success(message);
            setModal(null);
            reload();
        } catch (err) {
            toast.error(err);
        }
    };

    if (loading && !lead) return <Spinner />;
    if (!lead) return <ErrorBanner error={error} onRetry={reload} />;

    const editable = ["NEW", "CONTACTED", "QUALIFIED"].includes(lead.status);

    return (
        <>
            <Link href="/leads" className="mb-4 inline-flex items-center gap-1 text-sm text-slate-400 hover:text-white">
                <ArrowLeft className="h-4 w-4" /> Leads
            </Link>
            <PageHeader
                title={lead.fullName}
                subtitle={`${lead.jobTitle ?? "Cargo não informado"} · ${lead.companyName}`}
                actions={
                    <>
                        {editable && <Button variant="secondary" icon={Pencil} onClick={() => setModal("edit")}>Editar</Button>}
                        {lead.status === "NEW" && (
                            <Button icon={PhoneCall} onClick={() => run(() => api.leads.contacted(lead.id), "Lead marcado como contatado")}>Marcar contatado</Button>
                        )}
                        {lead.status === "CONTACTED" && (
                            <Button icon={CheckCheck} onClick={() => run(() => api.leads.qualify(lead.id), "Lead qualificado")}>Qualificar</Button>
                        )}
                        {lead.status === "QUALIFIED" && <Button icon={ArrowRightLeft} onClick={() => setModal("convert")}>Converter</Button>}
                        {editable && <Button variant="danger" icon={Ban} onClick={() => setModal("disqualify")}>Desqualificar</Button>}
                        {lead.status === "UNQUALIFIED" && (
                            <Button variant="secondary" icon={RotateCcw} onClick={() => run(() => api.leads.reopen(lead.id), "Lead reaberto")}>Reabrir</Button>
                        )}
                    </>
                }
            >
                <div className="mt-3 flex flex-wrap gap-2">
                    <LabeledBadge list={LEAD_STATUSES} value={lead.status} />
                    <Badge>{labelOf(LEAD_SOURCES, lead.source)}</Badge>
                </div>
            </PageHeader>

            {lead.status !== "UNQUALIFIED" && (
                <div className="card mb-6 px-5 py-4">
                    <FunnelStepper status={lead.status} />
                </div>
            )}

            {lead.status === "CONVERTING" && (
                <div className="mb-6 flex items-center gap-3 rounded-lg border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-sm text-amber-100">
                    <Loader2 className="h-4 w-4 animate-spin text-amber-400" />
                    Saga em andamento: aguardando o accounts-service provisionar a empresa e o contato (evento sales.lead.conversion-requested publicado).
                </div>
            )}
            {lead.conversionFailureReason && lead.status === "QUALIFIED" && (
                <div className="mb-6 rounded-lg border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-100">
                    A última conversão foi compensada (lead voltou para Qualificado): {lead.conversionFailureReason}
                </div>
            )}
            {lead.status === "CONVERTED" && (
                <div className="mb-6 flex flex-wrap items-center gap-3 rounded-lg border border-emerald-500/30 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-100">
                    Lead convertido em {dateTime(lead.convertedAt)}.
                    <Link href={`/companies/${lead.convertedCompanyId}`} className="font-medium underline">Ver empresa</Link>
                    {lead.convertedOpportunityId && (
                        <Link href={`/opportunities/${lead.convertedOpportunityId}`} className="font-medium underline">Ver oportunidade</Link>
                    )}
                </div>
            )}

            <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
                <div className="space-y-6">
                    <Section title="Score do lead">
                        <div className="flex items-center gap-4">
                            <ScoreRing score={lead.score} size={72} />
                            <p className="text-sm text-slate-400">Calculado a partir do contato, do cargo, da origem, do valor e da etapa.</p>
                        </div>
                        <ul className="mt-4 space-y-1.5">
                            {lead.scoreFactors?.map((factor) => (
                                <li key={factor.label} className="flex justify-between text-sm">
                                    <span className="text-slate-300">{factor.label}</span>
                                    <span className="font-medium text-emerald-300">+{factor.points}</span>
                                </li>
                            ))}
                        </ul>
                    </Section>
                    <Section title="Dados">
                        <DetailList
                            items={[
                                { label: "E-mail", value: lead.email },
                                { label: "Telefone", value: lead.phone },
                                { label: "Valor estimado", value: lead.estimatedValue ? money(lead.estimatedValue) : null },
                                { label: "Responsável", value: lead.ownerName ?? "Na fila" },
                                lead.disqualifyReason && { label: "Motivo da desqualificação", value: lead.disqualifyReason },
                                { label: "Criado por", value: `${lead.createdBy} em ${dateTime(lead.createdAt)}` },
                            ]}
                        />
                        {lead.notes && <p className="mt-4 rounded-lg bg-surface-850 p-3 text-sm text-slate-300">{lead.notes}</p>}
                        {editable && (
                            <div className="mt-5 flex gap-2 border-t border-surface-700 pt-4">
                                <Select
                                    className="flex-1"
                                    placeholder={lead.ownerId ? "Reatribuir para..." : "Atribuir para..."}
                                    options={activeSalesReps.filter((rep) => rep.id !== lead.ownerId).map((rep) => ({ value: rep.id, label: rep.name }))}
                                    value={assignee}
                                    onChange={(event) => setAssignee(event.target.value)}
                                />
                                <Button
                                    variant="secondary"
                                    icon={UserPlus}
                                    disabled={!assignee}
                                    onClick={() => run(() => api.leads.assign(lead.id, Number(assignee)), "Lead atribuído")}
                                >
                                    Atribuir
                                </Button>
                            </div>
                        )}
                        {editable && !lead.ownerId && user && (
                            <Button className="mt-3 w-full" variant="ghost" onClick={() => run(() => api.leads.assign(lead.id, user.id), "Lead atribuído a você")}>
                                Assumir este lead
                            </Button>
                        )}
                    </Section>
                </div>
                <div className="xl:col-span-2">
                    <Tabs
                        active={tab}
                        onChange={setTab}
                        tabs={[
                            { value: "activities", label: "Atividades" },
                            { value: "history", label: "Histórico" },
                        ]}
                    />
                    <div className="card p-5">
                        {tab === "activities" && <ActivityPanel relatedType="LEAD" relatedId={lead.id} />}
                        {tab === "history" && (
                            <HistoryTimeline
                                loader={() => api.leads.revisions(lead.id)}
                                describe={(data) => `${labelOf(LEAD_STATUSES, data.status)} · score ${data.score}${data.ownerName ? ` · ${data.ownerName}` : ""}`}
                            />
                        )}
                    </div>
                </div>
            </div>

            {modal === "edit" && <LeadForm lead={lead} onSaved={reload} onClose={() => setModal(null)} />}
            {modal === "convert" && <ConvertModal lead={lead} onConverted={reload} onClose={() => setModal(null)} />}
            {modal === "disqualify" && (
                <ReasonModal
                    title="Desqualificar lead"
                    label="Motivo"
                    confirm="Desqualificar"
                    onClose={() => setModal(null)}
                    onConfirm={(reason) => run(() => api.leads.disqualify(lead.id, reason), "Lead desqualificado")}
                />
            )}
        </>
    );
}
