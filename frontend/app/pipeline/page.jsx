"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import { DndContext, DragOverlay, KeyboardSensor, PointerSensor, useDraggable, useDroppable, useSensor, useSensors } from "@dnd-kit/core";
import { CalendarDays, Plus, ShieldAlert, ThumbsDown, Trophy } from "lucide-react";
import { api } from "@/lib/api";
import { useApi } from "@/lib/useApi";
import { OPEN_STAGES } from "@/lib/labels";
import { date, moneyCompact } from "@/lib/format";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import { useToast } from "@/components/providers/ToastProvider";
import Button from "@/components/ui/Button";
import Badge from "@/components/ui/Badge";
import Modal from "@/components/ui/Modal";
import { Select, Textarea } from "@/components/ui/Field";
import { ErrorBanner, Spinner } from "@/components/ui/Feedback";
import { Avatar, PageHeader } from "@/components/ui/Layout";
import OpportunityForm from "@/components/opportunities/OpportunityForm";

const COLUMN_ACCENTS = {
    PROSPECTING: "border-t-sky-500",
    QUALIFICATION: "border-t-cyan-500",
    PROPOSAL: "border-t-violet-500",
    NEGOTIATION: "border-t-amber-500",
};

function OpportunityCard({ opportunity, dragging = false }) {
    const overdue = new Date(`${opportunity.expectedCloseDate}T23:59:59`) < new Date();
    return (
        <div className={`rounded-lg border border-surface-700 bg-surface-850 p-3 shadow-md ${dragging ? "rotate-2 ring-2 ring-brand-500" : "hover:border-brand-500/40"}`}>
            <Link href={`/opportunities/${opportunity.id}`} className="block text-sm font-medium text-white hover:text-brand-300">
                {opportunity.title}
            </Link>
            <p className="mt-0.5 text-xs text-slate-400">{opportunity.companyName}</p>
            <div className="mt-3 flex items-center justify-between">
                <span className="text-sm font-semibold text-white">{moneyCompact(opportunity.amount)}</span>
                <span className="text-xs text-slate-400">{opportunity.probability}%</span>
            </div>
            <div className="mt-2 flex items-center justify-between gap-2">
                <span className={`inline-flex items-center gap-1 text-xs ${overdue ? "text-rose-300" : "text-slate-400"}`}>
                    <CalendarDays className="h-3 w-3" /> {date(opportunity.expectedCloseDate)}
                </span>
                <Avatar name={opportunity.ownerName} size="sm" />
            </div>
            {opportunity.discountApproval === "PENDING" && (
                <Badge color="amber" className="mt-2"><ShieldAlert className="h-3 w-3" /> Desconto pendente</Badge>
            )}
        </div>
    );
}

function DraggableCard({ opportunity }) {
    const { attributes, listeners, setNodeRef, isDragging } = useDraggable({ id: opportunity.id, data: opportunity });
    return (
        <div ref={setNodeRef} {...listeners} {...attributes} className={`cursor-grab touch-none ${isDragging ? "opacity-30" : ""}`}>
            <OpportunityCard opportunity={opportunity} />
        </div>
    );
}

function Column({ stage, opportunities }) {
    const { setNodeRef, isOver } = useDroppable({ id: stage.value });
    const total = opportunities.reduce((sum, opportunity) => sum + Number(opportunity.amount), 0);
    const weighted = opportunities.reduce((sum, opportunity) => sum + Number(opportunity.weightedAmount), 0);
    return (
        <div ref={setNodeRef} className={`flex min-h-[420px] w-72 shrink-0 flex-col rounded-xl border border-t-4 border-surface-700/70 bg-surface-900 ${COLUMN_ACCENTS[stage.value]} ${isOver ? "ring-2 ring-brand-500/60" : ""}`}>
            <div className="border-b border-surface-700/70 px-4 py-3">
                <div className="flex items-center justify-between">
                    <p className="font-semibold text-white">{stage.label}</p>
                    <span className="rounded-full bg-surface-800 px-2 py-0.5 text-xs text-slate-300">{opportunities.length}</span>
                </div>
                <p className="mt-1 text-xs text-slate-400">{moneyCompact(total)} · ponderado {moneyCompact(weighted)}</p>
            </div>
            <div className="flex-1 space-y-2 overflow-y-auto p-3">
                {opportunities.map((opportunity) => (
                    <DraggableCard key={opportunity.id} opportunity={opportunity} />
                ))}
            </div>
        </div>
    );
}

function OutcomeZone({ id, label, icon: Icon, tone }) {
    const { setNodeRef, isOver } = useDroppable({ id });
    const tones = {
        emerald: isOver ? "border-emerald-400 bg-emerald-500/20" : "border-emerald-500/40 bg-emerald-500/5",
        rose: isOver ? "border-rose-400 bg-rose-500/20" : "border-rose-500/40 bg-rose-500/5",
    };
    return (
        <div ref={setNodeRef} className={`flex flex-1 items-center justify-center gap-2 rounded-xl border-2 border-dashed py-5 text-sm font-medium text-slate-200 transition ${tones[tone]}`}>
            <Icon className="h-5 w-5" /> Solte aqui para marcar como {label}
        </div>
    );
}

export default function PipelinePage() {
    const toast = useToast();
    const { user, activeSalesReps } = useCurrentUser();
    const [ownerFilter, setOwnerFilter] = useState("");
    const [dragged, setDragged] = useState(null);
    const [losing, setLosing] = useState(null);
    const [lossReason, setLossReason] = useState("");
    const [creating, setCreating] = useState(false);
    const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 6 } }), useSensor(KeyboardSensor));
    const { data, loading, error, reload, setData } = useApi(
        () => api.opportunities.list({ open: true, ownerId: ownerFilter, pageSize: 100 }),
        [ownerFilter]
    );

    const byStage = useMemo(() => {
        const groups = Object.fromEntries(OPEN_STAGES.map((stage) => [stage.value, []]));
        data?.content.forEach((opportunity) => groups[opportunity.stage]?.push(opportunity));
        return groups;
    }, [data]);

    const totals = useMemo(() => {
        const list = data?.content ?? [];
        return {
            amount: list.reduce((sum, item) => sum + Number(item.amount), 0),
            weighted: list.reduce((sum, item) => sum + Number(item.weightedAmount), 0),
        };
    }, [data]);

    const optimisticMove = (id, stage) =>
        setData((current) => ({ ...current, content: current.content.map((item) => (item.id === id ? { ...item, stage } : item)) }));

    const onDragEnd = async ({ active, over }) => {
        setDragged(null);
        if (!over) return;
        const opportunity = active.data.current;
        if (over.id === opportunity.stage) return;
        if (over.id === "LOST") {
            setLosing(opportunity);
            return;
        }
        try {
            if (over.id === "WON") {
                await api.opportunities.win(opportunity.id);
                toast.success(`"${opportunity.title}" ganha! ${opportunity.companyName} vira cliente.`);
            } else {
                optimisticMove(opportunity.id, over.id);
                await api.opportunities.moveTo(opportunity.id, over.id);
                toast.success(`Movida para ${OPEN_STAGES.find((stage) => stage.value === over.id).label}`);
            }
        } catch (err) {
            toast.error(err);
        }
        reload();
    };

    const confirmLoss = async () => {
        try {
            await api.opportunities.lose(losing.id, lossReason);
            toast.success("Oportunidade marcada como perdida");
            setLosing(null);
            setLossReason("");
            reload();
        } catch (err) {
            toast.error(err);
        }
    };

    return (
        <>
            <PageHeader
                title="Pipeline"
                subtitle={`${data?.content.length ?? 0} oportunidades abertas · ${moneyCompact(totals.amount)} em pipeline · forecast ponderado ${moneyCompact(totals.weighted)}`}
                actions={
                    <>
                        <Select
                            className="w-56"
                            options={[
                                ...(user ? [{ value: String(user.id), label: "Minhas oportunidades" }] : []),
                                ...activeSalesReps.filter((rep) => rep.id !== user?.id).map((rep) => ({ value: String(rep.id), label: rep.name })),
                            ]}
                            placeholder="Toda a equipe"
                            value={ownerFilter}
                            onChange={(event) => setOwnerFilter(event.target.value)}
                        />
                        <Button icon={Plus} onClick={() => setCreating(true)}>Nova oportunidade</Button>
                    </>
                }
            />
            <ErrorBanner error={error} onRetry={reload} />
            {loading && !data ? (
                <Spinner />
            ) : (
                <DndContext sensors={sensors} onDragStart={({ active }) => setDragged(active.data.current)} onDragEnd={onDragEnd} onDragCancel={() => setDragged(null)}>
                    <div className="flex gap-4 overflow-x-auto pb-4">
                        {OPEN_STAGES.map((stage) => (
                            <Column key={stage.value} stage={stage} opportunities={byStage[stage.value]} />
                        ))}
                    </div>
                    <div className={`mt-4 flex flex-col gap-4 transition sm:flex-row ${dragged ? "opacity-100" : "opacity-40"}`}>
                        <OutcomeZone id="WON" label="ganha" icon={Trophy} tone="emerald" />
                        <OutcomeZone id="LOST" label="perdida" icon={ThumbsDown} tone="rose" />
                    </div>
                    <DragOverlay>{dragged ? <div className="w-72"><OpportunityCard opportunity={dragged} dragging /></div> : null}</DragOverlay>
                </DndContext>
            )}
            {losing && (
                <Modal
                    open
                    size="sm"
                    title={`Perder "${losing.title}"`}
                    onClose={() => setLosing(null)}
                    footer={
                        <>
                            <Button variant="secondary" onClick={() => setLosing(null)}>Cancelar</Button>
                            <Button variant="danger" disabled={!lossReason.trim()} onClick={confirmLoss}>Confirmar perda</Button>
                        </>
                    }
                >
                    <Textarea label="Motivo da perda" value={lossReason} onChange={(event) => setLossReason(event.target.value)} />
                </Modal>
            )}
            {creating && <OpportunityForm onSaved={reload} onClose={() => setCreating(false)} />}
        </>
    );
}
