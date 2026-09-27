"use client";

import { useState } from "react";
import { CalendarClock, CheckCircle2, Mail, Phone, Plus, RotateCcw, Users, XCircle } from "lucide-react";
import { api } from "@/lib/api";
import { useApi } from "@/lib/useApi";
import { ACTIVITY_STATUSES, ACTIVITY_TYPES, labelOf } from "@/lib/labels";
import { dateTime, timeAgo } from "@/lib/format";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import { useToast } from "@/components/providers/ToastProvider";
import Button from "@/components/ui/Button";
import { LabeledBadge } from "@/components/ui/Badge";
import Badge from "@/components/ui/Badge";
import Modal from "@/components/ui/Modal";
import { Input, Select, Textarea } from "@/components/ui/Field";
import { EmptyState, ErrorBanner, Spinner } from "@/components/ui/Feedback";

const ICONS = { TASK: CalendarClock, CALL: Phone, MEETING: Users, EMAIL: Mail };

function toLocalInput(date) {
    const offset = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - offset).toISOString().slice(0, 16);
}

function ActivityForm({ relatedType, relatedId, onCreated, onClose }) {
    const { user, activeSalesReps } = useCurrentUser();
    const toast = useToast();
    const tomorrow = new Date(Date.now() + 24 * 3600 * 1000);
    tomorrow.setMinutes(0, 0, 0);
    const [form, setForm] = useState({
        type: "CALL",
        subject: "",
        description: "",
        priority: "NORMAL",
        ownerId: user?.id ?? "",
        dueAt: toLocalInput(tomorrow),
        durationMinutes: 60,
        location: "",
    });
    const [saving, setSaving] = useState(false);
    const set = (field) => (event) => setForm((current) => ({ ...current, [field]: event.target.value }));

    const submit = async () => {
        setSaving(true);
        try {
            const start = new Date(form.dueAt);
            const body = {
                type: form.type,
                subject: form.subject,
                description: form.description,
                priority: form.priority,
                relatedType,
                relatedId,
                ownerId: Number(form.ownerId),
            };
            if (form.type === "MEETING") {
                body.startsAt = start.toISOString();
                body.endsAt = new Date(start.getTime() + Number(form.durationMinutes) * 60000).toISOString();
                body.location = form.location;
            } else {
                body.dueAt = start.toISOString();
            }
            await api.activities.create(body);
            toast.success("Atividade agendada");
            onCreated();
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
            title="Nova atividade"
            onClose={onClose}
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>Cancelar</Button>
                    <Button onClick={submit} loading={saving} disabled={!form.subject}>Agendar</Button>
                </>
            }
        >
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <Select label="Tipo" options={ACTIVITY_TYPES} value={form.type} onChange={set("type")} />
                <Select
                    label="Prioridade"
                    options={[{ value: "LOW", label: "Baixa" }, { value: "NORMAL", label: "Normal" }, { value: "HIGH", label: "Alta" }]}
                    value={form.priority}
                    onChange={set("priority")}
                />
                <Input className="sm:col-span-2" label="Assunto" value={form.subject} onChange={set("subject")} placeholder="Ex.: Apresentar proposta revisada" />
                <Input label={form.type === "MEETING" ? "Início" : "Prazo"} type="datetime-local" value={form.dueAt} onChange={set("dueAt")} />
                <Select
                    label="Responsável"
                    options={activeSalesReps.map((rep) => ({ value: rep.id, label: rep.name }))}
                    value={form.ownerId}
                    onChange={set("ownerId")}
                />
                {form.type === "MEETING" && (
                    <>
                        <Input label="Duração (min)" type="number" min="15" max="480" value={form.durationMinutes} onChange={set("durationMinutes")} />
                        <Input label="Local ou link" value={form.location} onChange={set("location")} />
                    </>
                )}
                <Textarea className="sm:col-span-2" label="Descrição" value={form.description} onChange={set("description")} />
            </div>
        </Modal>
    );
}

function CompleteForm({ activity, onDone, onClose }) {
    const toast = useToast();
    const [outcome, setOutcome] = useState("");
    const [minutes, setMinutes] = useState(activity.type === "CALL" ? 15 : "");
    const [saving, setSaving] = useState(false);

    const submit = async () => {
        setSaving(true);
        try {
            await api.activities.complete(activity.id, {
                outcome: outcome || null,
                durationMinutes: minutes === "" ? null : Number(minutes),
            });
            toast.success("Atividade concluída");
            onDone();
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
            size="sm"
            title="Concluir atividade"
            description={activity.subject}
            onClose={onClose}
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>Cancelar</Button>
                    <Button variant="success" onClick={submit} loading={saving}>Concluir</Button>
                </>
            }
        >
            <div className="space-y-4">
                <Textarea
                    label={activity.type === "CALL" || activity.type === "MEETING" ? "Resultado (obrigatório)" : "Resultado"}
                    value={outcome}
                    onChange={(event) => setOutcome(event.target.value)}
                />
                {activity.type === "CALL" && (
                    <Input label="Duração da ligação (min)" type="number" min="1" value={minutes} onChange={(event) => setMinutes(event.target.value)} />
                )}
            </div>
        </Modal>
    );
}

export default function ActivityPanel({ relatedType, relatedId }) {
    const toast = useToast();
    const [creating, setCreating] = useState(false);
    const [completing, setCompleting] = useState(null);
    const { data, loading, error, reload } = useApi(
        () => api.activities.list({ relatedType, relatedId, pageSize: 50 }),
        [relatedType, relatedId]
    );

    const act = async (action, message) => {
        try {
            await action();
            toast.success(message);
            reload();
        } catch (err) {
            toast.error(err);
        }
    };

    return (
        <div>
            <div className="mb-4 flex justify-end">
                <Button size="sm" icon={Plus} onClick={() => setCreating(true)}>Nova atividade</Button>
            </div>
            {loading && <Spinner />}
            <ErrorBanner error={error} onRetry={reload} />
            {!loading && data?.content.length === 0 && (
                <EmptyState icon={CalendarClock} title="Nenhuma atividade" description="Agende ligações, reuniões e tarefas para não perder o timing." />
            )}
            <ul className="space-y-2">
                {data?.content.map((activity) => {
                    const Icon = ICONS[activity.type] ?? CalendarClock;
                    return (
                        <li key={activity.id} className={`flex items-start gap-3 rounded-lg border p-3 ${activity.overdue ? "border-rose-500/40 bg-rose-500/5" : "border-surface-700 bg-surface-850"}`}>
                            <div className="rounded-md bg-surface-800 p-2 text-brand-400">
                                <Icon className="h-4 w-4" />
                            </div>
                            <div className="min-w-0 flex-1">
                                <div className="flex flex-wrap items-center gap-2">
                                    <p className="font-medium text-white">{activity.subject}</p>
                                    <LabeledBadge list={ACTIVITY_STATUSES} value={activity.status} />
                                    {activity.overdue && <Badge color="rose">Atrasada</Badge>}
                                    {activity.priority === "HIGH" && <Badge color="amber">Alta prioridade</Badge>}
                                </div>
                                <p className="mt-0.5 text-xs text-slate-400">
                                    {labelOf(ACTIVITY_TYPES, activity.type)} · {dateTime(activity.startsAt ?? activity.dueAt)} ({timeAgo(activity.startsAt ?? activity.dueAt)}) · {activity.ownerName}
                                    {activity.location && ` · ${activity.location}`}
                                </p>
                                {activity.outcome && <p className="mt-1 text-sm text-emerald-300/90">Resultado: {activity.outcome}</p>}
                            </div>
                            <div className="flex gap-1">
                                {activity.status === "PLANNED" ? (
                                    <>
                                        <button title="Concluir" onClick={() => setCompleting(activity)} className="rounded-md p-1.5 text-emerald-400 hover:bg-surface-800">
                                            <CheckCircle2 className="h-4 w-4" />
                                        </button>
                                        <button
                                            title="Cancelar"
                                            onClick={() => act(() => api.activities.cancel(activity.id, "cancelada pelo usuário"), "Atividade cancelada")}
                                            className="rounded-md p-1.5 text-slate-400 hover:bg-surface-800"
                                        >
                                            <XCircle className="h-4 w-4" />
                                        </button>
                                    </>
                                ) : (
                                    <button title="Reabrir" onClick={() => act(() => api.activities.reopen(activity.id), "Atividade reaberta")} className="rounded-md p-1.5 text-slate-400 hover:bg-surface-800">
                                        <RotateCcw className="h-4 w-4" />
                                    </button>
                                )}
                            </div>
                        </li>
                    );
                })}
            </ul>
            {creating && <ActivityForm relatedType={relatedType} relatedId={relatedId} onCreated={reload} onClose={() => setCreating(false)} />}
            {completing && <CompleteForm activity={completing} onDone={reload} onClose={() => setCompleting(null)} />}
        </div>
    );
}
