"use client";

import { useState } from "react";
import { api } from "@/lib/api";
import { DECISION_ROLES } from "@/lib/labels";
import { useToast } from "@/components/providers/ToastProvider";
import Button from "@/components/ui/Button";
import Modal from "@/components/ui/Modal";
import { Input, Select } from "@/components/ui/Field";

const EMPTY = {
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    mobile: "",
    jobTitle: "",
    department: "",
    decisionRole: "INFLUENCER",
    primary: false,
    active: true,
};

export default function ContactForm({ companyId, companies, contact, onSaved, onClose }) {
    const toast = useToast();
    const [saving, setSaving] = useState(false);
    const [form, setForm] = useState(() =>
        contact
            ? { ...EMPTY, ...Object.fromEntries(Object.entries(contact).map(([key, value]) => [key, value ?? ""])) }
            : { ...EMPTY, companyId: companyId ?? "" }
    );
    const set = (field) => (event) => setForm((current) => ({ ...current, [field]: event.target.value }));
    const toggle = (field) => (event) => setForm((current) => ({ ...current, [field]: event.target.checked }));

    const submit = async () => {
        setSaving(true);
        const body = { ...form, companyId: Number(form.companyId), version: contact?.version };
        try {
            const saved = contact ? await api.contacts.update(contact.id, body) : await api.contacts.create(body);
            toast.success(contact ? "Contato atualizado" : "Contato cadastrado");
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
            title={contact ? `Editar ${contact.fullName}` : "Novo contato"}
            onClose={onClose}
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>Cancelar</Button>
                    <Button onClick={submit} loading={saving} disabled={!form.firstName || !form.lastName || !form.email || !form.companyId}>
                        Salvar
                    </Button>
                </>
            }
        >
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                {companies && !contact && (
                    <Select
                        className="sm:col-span-2"
                        label="Empresa"
                        placeholder="Selecione a empresa"
                        options={companies.map((company) => ({ value: company.id, label: company.displayName }))}
                        value={form.companyId}
                        onChange={set("companyId")}
                    />
                )}
                <Input label="Nome" value={form.firstName} onChange={set("firstName")} />
                <Input label="Sobrenome" value={form.lastName} onChange={set("lastName")} />
                <Input className="sm:col-span-2" label="E-mail corporativo" type="email" value={form.email} onChange={set("email")} />
                <Input label="Telefone" value={form.phone} onChange={set("phone")} />
                <Input label="Celular" value={form.mobile} onChange={set("mobile")} />
                <Input label="Cargo" value={form.jobTitle} onChange={set("jobTitle")} />
                <Input label="Departamento" value={form.department} onChange={set("department")} />
                <Select className="sm:col-span-2" label="Papel na decisão" options={DECISION_ROLES} value={form.decisionRole} onChange={set("decisionRole")} />
                <label className="flex items-center gap-2 text-sm text-slate-300">
                    <input type="checkbox" checked={form.primary} onChange={toggle("primary")} className="accent-sky-500" />
                    Contato principal da empresa
                </label>
                {contact && (
                    <label className="flex items-center gap-2 text-sm text-slate-300">
                        <input type="checkbox" checked={form.active} onChange={toggle("active")} className="accent-sky-500" />
                        Ainda trabalha na empresa
                    </label>
                )}
            </div>
        </Modal>
    );
}
