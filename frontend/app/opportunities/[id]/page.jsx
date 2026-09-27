"use client";

import Link from "next/link";
import { use, useMemo, useState } from "react";
import { ArrowLeft, Check, Pencil, Plus, RotateCcw, ShieldAlert, ThumbsDown, Trash2, Trophy, X } from "lucide-react";
import { api } from "@/lib/api";
import { useApi } from "@/lib/useApi";
import { BILLING, DISCOUNT_APPROVAL, OPEN_STAGES, OPPORTUNITY_STAGES, PRODUCT_CATEGORIES, labelOf } from "@/lib/labels";
import { date, dateTime, money, percent } from "@/lib/format";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import { useToast } from "@/components/providers/ToastProvider";
import Button from "@/components/ui/Button";
import Badge, { LabeledBadge } from "@/components/ui/Badge";
import Modal from "@/components/ui/Modal";
import { Input, Select, Textarea } from "@/components/ui/Field";
import { EmptyState, ErrorBanner, Spinner } from "@/components/ui/Feedback";
import { DetailList, KpiCard, PageHeader, Section, Tabs } from "@/components/ui/Layout";
import ActivityPanel from "@/components/shared/ActivityPanel";
import HistoryTimeline from "@/components/shared/HistoryTimeline";
import OpportunityForm from "@/components/opportunities/OpportunityForm";

function StageBar({ opportunity, onMove }) {
    const currentIndex = OPEN_STAGES.findIndex((stage) => stage.value === opportunity.stage);
    return (
        <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
            {OPEN_STAGES.map((stage, index) => {
                const active = stage.value === opportunity.stage;
                const passed = opportunity.open ? index < currentIndex : true;
                return (
                    <button
                        key={stage.value}
                        disabled={!opportunity.open || active}
                        onClick={() => onMove(stage.value)}
                        className={`rounded-lg px-3 py-2.5 text-sm font-medium transition disabled:cursor-default ${
                            active
                                ? "bg-brand-500 text-white shadow-lg shadow-brand-500/30"
                                : passed
                                  ? "bg-brand-500/15 text-brand-200 hover:bg-brand-500/25"
                                  : "bg-surface-800 text-slate-400 hover:bg-surface-700 hover:text-white"
                        }`}
                    >
                        {stage.label}
                    </button>
                );
            })}
        </div>
    );
}

function AddItemModal({ opportunity, onAdded, onClose }) {
    const toast = useToast();
    const [category, setCategory] = useState("");
    const [productId, setProductId] = useState("");
    const [quantity, setQuantity] = useState(1);
    const [discount, setDiscount] = useState(0);
    const [saving, setSaving] = useState(false);
    const { data } = useApi(() => api.products.list({ active: true, category }), [category]);
    const product = data?.content.find((entry) => entry.id === Number(productId));
    const aboveLimit = product && Number(discount) > Number(product.maxDiscountPercent);

    const submit = async () => {
        setSaving(true);
        try {
            await api.opportunities.addItem(opportunity.id, { productId: Number(productId), quantity: Number(quantity), discountPercent: Number(discount) });
            toast.success(aboveLimit ? "Item adicionado: desconto acima do limite enviado para aprovação do gestor" : "Item adicionado");
            onAdded();
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
            title="Adicionar item"
            description="Preço e desconto máximo vêm do catálogo (réplica local sincronizada por eventos)."
            onClose={onClose}
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>Cancelar</Button>
                    <Button onClick={submit} loading={saving} disabled={!productId}>Adicionar</Button>
                </>
            }
        >
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <Select label="Categoria" options={PRODUCT_CATEGORIES} placeholder="Todas" value={category} onChange={(event) => { setCategory(event.target.value); setProductId(""); }} />
                <Select
                    label="Produto"
                    placeholder="Selecione"
                    options={(data?.content ?? []).map((entry) => ({ value: entry.id, label: `${entry.name} · ${money(entry.unitPrice)}` }))}
                    value={productId}
                    onChange={(event) => setProductId(event.target.value)}
                />
                <Input label="Quantidade" type="number" min="1" value={quantity} onChange={(event) => setQuantity(event.target.value)} />
                <Input label="Desconto (%)" type="number" min="0" max="100" step="0.5" value={discount} onChange={(event) => setDiscount(event.target.value)} />
                {product && (
                    <div className="rounded-lg bg-surface-850 p-3 text-sm text-slate-300 sm:col-span-2">
                        <p>{product.sku} · {labelOf(BILLING, product.billing)} · margem {percent(product.marginPercent)}</p>
                        <p className="mt-1">
                            Subtotal: <span className="font-semibold text-white">{money(product.unitPrice * quantity * (1 - discount / 100))}</span>
                            {" · "}desconto máximo sem aprovação: {percent(product.maxDiscountPercent)}
                        </p>
                        {aboveLimit && <p className="mt-2 text-amber-300">Acima do limite: a oportunidade ficará aguardando aprovação do gestor.</p>}
                    </div>
                )}
            </div>
        </Modal>
    );
}

function DiscountApprovalPanel({ opportunity, canDecide, onDecided }) {
    const { user } = useCurrentUser();
    const toast = useToast();
    const [comment, setComment] = useState("");
    const decide = async (approved) => {
        try {
            await api.opportunities.decideDiscount(opportunity.id, { approverId: user.id, approved, comment });
            toast.success(approved ? "Desconto aprovado" : "Desconto rejeitado");
            onDecided();
        } catch (error) {
            toast.error(error);
        }
    };
    return (
        <div className="mb-6 rounded-xl border border-amber-500/30 bg-amber-500/10 p-5">
            <div className="flex items-start gap-3">
                <ShieldAlert className="mt-0.5 h-5 w-5 text-amber-400" />
                <div className="flex-1">
                    <p className="font-medium text-amber-100">Desconto acima do limite do catálogo aguardando aprovação</p>
                    <p className="text-sm text-amber-200/80">
                        {canDecide
                            ? "Você é o gestor do vendedor responsável: aprove ou rejeite abaixo."
                            : "Somente o gestor do vendedor responsável pode decidir. Troque o usuário no topo para simular."}
                    </p>
                    {canDecide && (
                        <div className="mt-3 flex flex-col gap-2 sm:flex-row">
                            <input className="input flex-1" placeholder="Comentário (obrigatório para rejeitar)" value={comment} onChange={(event) => setComment(event.target.value)} />
                            <Button variant="success" icon={Check} onClick={() => decide(true)}>Aprovar</Button>
                            <Button variant="danger" icon={X} disabled={!comment.trim()} onClick={() => decide(false)}>Rejeitar</Button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default function OpportunityDetailPage({ params }) {
    const { id } = use(params);
    const toast = useToast();
    const { user, salesReps } = useCurrentUser();
    const [tab, setTab] = useState("items");
    const [modal, setModal] = useState(null);
    const [lossReason, setLossReason] = useState("");
    const { data: opportunity, loading, error, reload } = useApi(() => api.opportunities.get(id), [id]);

    const owner = useMemo(() => salesReps.find((rep) => rep.id === opportunity?.ownerId), [salesReps, opportunity?.ownerId]);
    const canDecide = Boolean(user && owner && owner.managerId === user.id);

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

    if (loading && !opportunity) return <Spinner />;
    if (!opportunity) return <ErrorBanner error={error} onRetry={reload} />;

    return (
        <>
            <Link href="/opportunities" className="mb-4 inline-flex items-center gap-1 text-sm text-slate-400 hover:text-white">
                <ArrowLeft className="h-4 w-4" /> Oportunidades
            </Link>
            <PageHeader
                title={opportunity.title}
                subtitle={
                    <span>
                        <Link href={`/companies/${opportunity.companyId}`} className="text-brand-300 hover:underline">{opportunity.companyName}</Link>
                        {opportunity.contactName && ` · ${opportunity.contactName}`} · responsável {opportunity.ownerName}
                    </span>
                }
                actions={
                    opportunity.open ? (
                        <>
                            <Button variant="secondary" icon={Pencil} onClick={() => setModal("edit")}>Editar</Button>
                            <Button variant="danger" icon={ThumbsDown} onClick={() => setModal("lose")}>Perdida</Button>
                            <Button variant="success" icon={Trophy} onClick={() => run(() => api.opportunities.win(opportunity.id), "Oportunidade ganha! A empresa vira cliente no accounts-service.")}>
                                Ganha
                            </Button>
                        </>
                    ) : (
                        <Button variant="secondary" icon={RotateCcw} onClick={() => run(() => api.opportunities.reopen(opportunity.id), "Oportunidade reaberta")}>Reabrir</Button>
                    )
                }
            >
                <div className="mt-3 flex flex-wrap gap-2">
                    <LabeledBadge list={OPPORTUNITY_STAGES} value={opportunity.stage} />
                    <LabeledBadge list={DISCOUNT_APPROVAL} value={opportunity.discountApproval} />
                    {opportunity.leadId && (
                        <Link href={`/leads/${opportunity.leadId}`}><Badge color="brand">Originada do lead #{opportunity.leadId}</Badge></Link>
                    )}
                </div>
            </PageHeader>

            {opportunity.discountApproval === "PENDING" && opportunity.open && (
                <DiscountApprovalPanel opportunity={opportunity} canDecide={canDecide} onDecided={reload} />
            )}
            {opportunity.discountApproval === "REJECTED" && (
                <div className="mb-6 rounded-lg border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-100">
                    Desconto rejeitado pelo gestor{opportunity.approvalComment && `: ${opportunity.approvalComment}`}. Ajuste os itens para seguir.
                </div>
            )}
            {opportunity.stage === "LOST" && (
                <div className="mb-6 rounded-lg border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-100">
                    Perdida em {dateTime(opportunity.closedAt)}. Motivo: {opportunity.lossReason}
                </div>
            )}

            <div className="mb-6 grid grid-cols-2 gap-4 lg:grid-cols-4">
                <KpiCard label="Valor total do contrato" value={money(opportunity.amount)} hint={`${opportunity.contractTermMonths} meses de contrato`} />
                <KpiCard label="Valor único" value={money(opportunity.oneTimeValue)} hint="Hardware, projetos e licenças" />
                <KpiCard label="MRR" value={money(opportunity.monthlyRecurringValue)} hint="Receita recorrente mensal" tone="violet" />
                <KpiCard label="Forecast ponderado" value={money(opportunity.weightedAmount)} hint={`Probabilidade de ${percent(opportunity.probability)}`} tone="emerald" />
            </div>

            {opportunity.open && (
                <div className="card mb-6 p-5">
                    <div className="mb-3 flex flex-wrap items-center justify-between gap-3">
                        <p className="text-sm font-semibold text-white">Etapa do funil</p>
                        <label className="flex items-center gap-2 text-sm text-slate-400">
                            Probabilidade
                            <input
                                type="number"
                                min="1"
                                max="99"
                                defaultValue={opportunity.probability}
                                key={`${opportunity.stage}-${opportunity.probability}`}
                                className="input w-20"
                                onBlur={(event) => {
                                    const value = Number(event.target.value);
                                    if (value !== opportunity.probability) {
                                        run(() => api.opportunities.probability(opportunity.id, value), "Probabilidade ajustada");
                                    }
                                }}
                            />
                            %
                        </label>
                    </div>
                    <StageBar opportunity={opportunity} onMove={(stage) => run(() => api.opportunities.moveTo(opportunity.id, stage), `Movida para ${labelOf(OPPORTUNITY_STAGES, stage)}`)} />
                </div>
            )}

            <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
                <div className="xl:col-span-2">
                    <Tabs
                        active={tab}
                        onChange={setTab}
                        tabs={[
                            { value: "items", label: "Itens", count: opportunity.items.length },
                            { value: "activities", label: "Atividades" },
                            { value: "stages", label: "Etapas" },
                            { value: "history", label: "Auditoria" },
                        ]}
                    />
                    <div className="card p-5">
                        {tab === "items" && (
                            <>
                                {opportunity.open && (
                                    <div className="mb-4 flex justify-end">
                                        <Button size="sm" icon={Plus} onClick={() => setModal("item")}>Adicionar item</Button>
                                    </div>
                                )}
                                {opportunity.items.length === 0 ? (
                                    <EmptyState title="Nenhum item" description="Adicione produtos do catálogo para calcular o valor e o MRR." />
                                ) : (
                                    <div className="overflow-x-auto">
                                        <table className="w-full text-sm">
                                            <thead>
                                                <tr className="table-head">
                                                    <th className="px-3 py-2">Produto</th>
                                                    <th className="px-3 py-2 text-right">Qtd</th>
                                                    <th className="px-3 py-2 text-right">Preço</th>
                                                    <th className="px-3 py-2 text-right">Desconto</th>
                                                    <th className="px-3 py-2 text-right">Total</th>
                                                    <th className="px-3 py-2" />
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {opportunity.items.map((item) => (
                                                    <tr key={item.id} className="table-row">
                                                        <td className="px-3 py-2">
                                                            <p className="font-medium text-white">{item.productName}</p>
                                                            <p className="text-xs text-slate-500">
                                                                {item.sku} · {labelOf(BILLING, item.billing)}
                                                                {item.billing !== "ONE_TIME" && ` · ${money(item.monthlyValue)}/mês`}
                                                            </p>
                                                        </td>
                                                        <td className="px-3 py-2 text-right text-slate-300">{item.quantity}</td>
                                                        <td className="px-3 py-2 text-right text-slate-300">{money(item.unitPrice)}</td>
                                                        <td className={`px-3 py-2 text-right ${item.aboveDiscountLimit ? "font-semibold text-amber-300" : "text-slate-300"}`}>
                                                            {percent(item.discountPercent)}
                                                            <span className="block text-[11px] font-normal text-slate-500">máx. {percent(item.maxDiscountPercent)}</span>
                                                        </td>
                                                        <td className="px-3 py-2 text-right font-medium text-white">{money(item.netTotal)}</td>
                                                        <td className="px-3 py-2 text-right">
                                                            {opportunity.open && (
                                                                <button
                                                                    title="Remover"
                                                                    onClick={() => run(() => api.opportunities.removeItem(opportunity.id, item.id), "Item removido")}
                                                                    className="rounded-md p-1.5 text-slate-500 hover:bg-surface-800 hover:text-rose-400"
                                                                >
                                                                    <Trash2 className="h-4 w-4" />
                                                                </button>
                                                            )}
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    </div>
                                )}
                            </>
                        )}
                        {tab === "activities" && <ActivityPanel relatedType="OPPORTUNITY" relatedId={opportunity.id} />}
                        {tab === "stages" && (
                            <ol className="relative space-y-4 border-l border-surface-700 pl-6">
                                {[...opportunity.stageHistory].reverse().map((change) => (
                                    <li key={change.id ?? change.changedAt} className="relative">
                                        <span className="absolute -left-[31px] top-1 h-3 w-3 rounded-full border-2 border-surface-900 bg-brand-500" />
                                        <p className="text-sm text-white">
                                            {change.fromStage ? `${labelOf(OPPORTUNITY_STAGES, change.fromStage)} → ` : ""}
                                            <span className="font-semibold">{labelOf(OPPORTUNITY_STAGES, change.toStage)}</span> ({change.probability}%)
                                        </p>
                                        <p className="text-xs text-slate-400">{dateTime(change.changedAt)} por {change.changedBy}{change.reason && ` · ${change.reason}`}</p>
                                    </li>
                                ))}
                            </ol>
                        )}
                        {tab === "history" && (
                            <HistoryTimeline
                                loader={() => api.opportunities.revisions(opportunity.id)}
                                describe={(data) => `${labelOf(OPPORTUNITY_STAGES, data.stage)} · ${money(data.amount)} · desconto ${labelOf(DISCOUNT_APPROVAL, data.discountApproval).toLowerCase()}`}
                            />
                        )}
                    </div>
                </div>
                <Section title="Detalhes">
                    <DetailList
                        items={[
                            { label: "Fechamento previsto", value: date(opportunity.expectedCloseDate) },
                            { label: "Prazo do contrato", value: `${opportunity.contractTermMonths} meses` },
                            { label: "Valor estimado", value: opportunity.estimatedValue ? money(opportunity.estimatedValue) : null },
                            opportunity.closedAt && { label: "Encerrada em", value: dateTime(opportunity.closedAt) },
                            opportunity.approvalDecidedAt && {
                                label: "Decisão do desconto",
                                value: `${labelOf(DISCOUNT_APPROVAL, opportunity.discountApproval)} em ${dateTime(opportunity.approvalDecidedAt)}`,
                            },
                            { label: "Criada por", value: `${opportunity.createdBy} em ${dateTime(opportunity.createdAt)}` },
                        ]}
                    />
                    {opportunity.description && <p className="mt-4 rounded-lg bg-surface-850 p-3 text-sm text-slate-300">{opportunity.description}</p>}
                </Section>
            </div>

            {modal === "edit" && <OpportunityForm opportunity={opportunity} onSaved={reload} onClose={() => setModal(null)} />}
            {modal === "item" && <AddItemModal opportunity={opportunity} onAdded={reload} onClose={() => setModal(null)} />}
            {modal === "lose" && (
                <Modal
                    open
                    size="sm"
                    title="Marcar como perdida"
                    onClose={() => setModal(null)}
                    footer={
                        <>
                            <Button variant="secondary" onClick={() => setModal(null)}>Cancelar</Button>
                            <Button variant="danger" disabled={!lossReason.trim()} onClick={() => run(() => api.opportunities.lose(opportunity.id, lossReason), "Oportunidade marcada como perdida")}>
                                Confirmar perda
                            </Button>
                        </>
                    }
                >
                    <Textarea label="Motivo da perda" value={lossReason} onChange={(event) => setLossReason(event.target.value)} placeholder="Ex.: preço acima do concorrente" />
                </Modal>
            )}
        </>
    );
}
