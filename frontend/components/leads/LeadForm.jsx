"use client";

import { useState } from "react";
import { api } from "@/lib/api";
import { LEAD_SOURCES } from "@/lib/labels";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import { useToast } from "@/components/providers/ToastProvider";
import Button from "@/components/ui/Button";
import Modal from "@/components/ui/Modal";
import { Input, Select, Textarea } from "@/components/ui/Field";

const EMPTY = {
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    companyName: "",
    jobTitle: "",
    source: "WEBSITE",
    estimatedValue: "",
    notes: "",
    ownerId: "",
};

export default function LeadForm({ lead, onSaved, onClose }) {
    const { activeSalesReps } = useCurrentUser();
    const toast = useToast();
    const [saving, setSaving] = useState(false);
    const [form, setForm] = useState(() =>
        lead ? { ...EMPTY, ...Object.fromEntries(Object.keys(EMPTY).map((key) => [key, lead[key] ?? ""])) } : EMPTY
    );
    const set = (field) => (event) => setForm((current) => ({ ...current, [field]: event.target.value }));

    const submit = async () => {
        setSaving(true);
        const body = {
            ...form,
            estimatedValue: form.estimatedValue === "" ? null : Number(form.estimatedValue),
            ownerId: form.ownerId === "" ? null : Number(form.ownerId),
            email: form.email || null,
            version: lead?.version,
        };
        try {
            const saved = lead ? await api.leads.update(lead.id, body) : await api.leads.create(body);
            toast.success(lead ? "Lead atualizado" : `Lead criado com score ${saved.score}`);
            onSaved(saved);
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
            title={lead ? `Editar ${lead.fullName}` : "Novo lead"}
            description="O score é calculado automaticamente a partir dos dados informados."
            onClose={onClose}
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>Cancelar</Button>
                    <Button onClick={submit} loading={saving} disabled={!form.firstName || !form.lastName || !form.companyName}>Salvar</Button>
                </>
            }
        >
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <Input label="Nome" value={form.firstName} onChange={set("firstName")} />
                <Input label="Sobrenome" value={form.lastName} onChange={set("lastName")} />
                <Input label="E-mail" type="email" value={form.email} onChange={set("email")} />
                <Input label="Telefone" value={form.phone} onChange={set("phone")} />
                <Input label="Empresa" value={form.companyName} onChange={set("companyName")} />
                <Input label="Cargo" value={form.jobTitle} onChange={set("jobTitle")} placeholder="Ex.: Diretor de TI" />
                <Select label="Origem" options={LEAD_SOURCES} value={form.source} onChange={set("source")} />
                <Input label="Valor estimado (R$)" type="number" min="0" value={form.estimatedValue} onChange={set("estimatedValue")} />
                {!lead && (
                    <Select
                        className="sm:col-span-2"
                        label="Vendedor responsável"
                        placeholder="Deixar na fila (sem responsável)"
                        options={activeSalesReps.map((rep) => ({ value: rep.id, label: rep.name }))}
                        value={form.ownerId}
                        onChange={set("ownerId")}
                    />
                )}
                <Textarea className="sm:col-span-2" label="Observações" value={form.notes} onChange={set("notes")} />
            </div>
        </Modal>
    );
}
