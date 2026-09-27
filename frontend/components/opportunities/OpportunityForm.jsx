"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { addDays } from "@/lib/format";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import { useToast } from "@/components/providers/ToastProvider";
import Button from "@/components/ui/Button";
import Modal from "@/components/ui/Modal";
import { Input, Select, Textarea } from "@/components/ui/Field";

export default function OpportunityForm({ opportunity, onSaved, onClose }) {
    const { user, activeSalesReps } = useCurrentUser();
    const toast = useToast();
    const [saving, setSaving] = useState(false);
    const [companies, setCompanies] = useState([]);
    const [contacts, setContacts] = useState([]);
    const [form, setForm] = useState(() => ({
        title: opportunity?.title ?? "",
        description: opportunity?.description ?? "",
        companyId: opportunity?.companyId ?? "",
        contactId: opportunity?.contactId ?? "",
        ownerId: opportunity?.ownerId ?? user?.id ?? "",
        expectedCloseDate: opportunity?.expectedCloseDate ?? addDays(30),
        contractTermMonths: opportunity?.contractTermMonths ?? 12,
        estimatedValue: opportunity?.estimatedValue ?? "",
    }));
    const set = (field) => (event) => setForm((current) => ({ ...current, [field]: event.target.value }));

    useEffect(() => {
        api.companies.list({ pageSize: 100 }).then((page) => setCompanies(page.content)).catch(() => setCompanies([]));
    }, []);

    useEffect(() => {
        if (!form.companyId) {
            setContacts([]);
            return;
        }
        api.contacts
            .list({ companyId: form.companyId, active: true, pageSize: 50 })
            .then((page) => setContacts(page.content))
            .catch(() => setContacts([]));
    }, [form.companyId]);

    const submit = async () => {
        setSaving(true);
        const body = {
            ...form,
            companyId: Number(form.companyId),
            contactId: form.contactId === "" ? null : Number(form.contactId),
            ownerId: Number(form.ownerId),
            contractTermMonths: Number(form.contractTermMonths),
            estimatedValue: form.estimatedValue === "" ? null : Number(form.estimatedValue),
            version: opportunity?.version,
        };
        try {
            const saved = opportunity ? await api.opportunities.update(opportunity.id, body) : await api.opportunities.create(body);
            toast.success(opportunity ? "Oportunidade atualizada" : "Oportunidade criada em Prospecção");
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
            size="lg"
            title={opportunity ? "Editar oportunidade" : "Nova oportunidade"}
            onClose={onClose}
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>Cancelar</Button>
                    <Button onClick={submit} loading={saving} disabled={!form.title || !form.companyId || !form.ownerId}>Salvar</Button>
                </>
            }
        >
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <Input className="sm:col-span-2" label="Título" value={form.title} onChange={set("title")} placeholder="Ex.: Renovação do parque de notebooks" />
                <Select
                    label="Empresa"
                    placeholder="Selecione"
                    disabled={Boolean(opportunity)}
                    options={companies.map((company) => ({ value: company.id, label: company.displayName }))}
                    value={form.companyId}
                    onChange={(event) => setForm((current) => ({ ...current, companyId: event.target.value, contactId: "" }))}
                />
                <Select
                    label="Contato"
                    placeholder="Sem contato"
                    options={contacts.map((contact) => ({ value: contact.id, label: `${contact.fullName}${contact.jobTitle ? ` (${contact.jobTitle})` : ""}` }))}
                    value={form.contactId}
                    onChange={set("contactId")}
                />
                <Select
                    label="Responsável"
                    options={activeSalesReps.map((rep) => ({ value: rep.id, label: rep.name }))}
                    value={form.ownerId}
                    onChange={set("ownerId")}
                />
                <Input label="Fechamento previsto" type="date" value={form.expectedCloseDate} onChange={set("expectedCloseDate")} />
                <Input label="Prazo do contrato (meses)" type="number" min="1" max="60" value={form.contractTermMonths} onChange={set("contractTermMonths")} hint="Multiplica a receita recorrente no valor total" />
                <Input label="Valor estimado (R$)" type="number" min="0" value={form.estimatedValue} onChange={set("estimatedValue")} hint="Usado enquanto não houver itens" />
                <Textarea className="sm:col-span-2" label="Descrição" value={form.description} onChange={set("description")} />
            </div>
        </Modal>
    );
}
