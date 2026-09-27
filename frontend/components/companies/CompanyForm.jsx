"use client";

import { useState } from "react";
import { api } from "@/lib/api";
import { COMPANY_SIZES, COMPANY_TYPES, INDUSTRIES, STATES } from "@/lib/labels";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import { useToast } from "@/components/providers/ToastProvider";
import Button from "@/components/ui/Button";
import Modal from "@/components/ui/Modal";
import { Input, Select, Textarea } from "@/components/ui/Field";

const EMPTY = {
    legalName: "",
    tradeName: "",
    cnpj: "",
    industry: "TECHNOLOGY",
    size: "MEDIUM",
    employees: "",
    annualRevenue: "",
    website: "",
    phone: "",
    city: "",
    state: "SP",
    type: "PROSPECT",
    ownerId: "",
    notes: "",
};

export default function CompanyForm({ company, onSaved, onClose }) {
    const { user, activeSalesReps } = useCurrentUser();
    const toast = useToast();
    const [saving, setSaving] = useState(false);
    const [form, setForm] = useState(() =>
        company
            ? {
                  ...EMPTY,
                  ...Object.fromEntries(Object.entries(company).map(([key, value]) => [key, value ?? ""])),
                  website: company.website?.replace(/^https?:\/\//, "") ?? "",
              }
            : { ...EMPTY, ownerId: user?.id ?? "" }
    );
    const set = (field) => (event) => setForm((current) => ({ ...current, [field]: event.target.value }));

    const submit = async () => {
        setSaving(true);
        const body = {
            ...form,
            employees: form.employees === "" ? null : Number(form.employees),
            annualRevenue: form.annualRevenue === "" ? null : Number(form.annualRevenue),
            ownerId: form.ownerId === "" ? null : Number(form.ownerId),
            state: form.state || null,
            version: company?.version,
        };
        try {
            const saved = company ? await api.companies.update(company.id, body) : await api.companies.create(body);
            toast.success(company ? "Empresa atualizada" : "Empresa cadastrada");
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
            title={company ? `Editar ${company.displayName}` : "Nova empresa"}
            onClose={onClose}
            footer={
                <>
                    <Button variant="secondary" onClick={onClose}>Cancelar</Button>
                    <Button onClick={submit} loading={saving} disabled={!form.legalName || !form.cnpj}>Salvar</Button>
                </>
            }
        >
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <Input label="Razão social" value={form.legalName} onChange={set("legalName")} />
                <Input label="Nome fantasia" value={form.tradeName} onChange={set("tradeName")} />
                <Input label="CNPJ" value={form.cnpj} onChange={set("cnpj")} placeholder="00.000.000/0000-00" hint="Aceita CNPJ numérico ou alfanumérico" />
                <Select label="Tipo de relacionamento" options={COMPANY_TYPES} value={form.type} onChange={set("type")} />
                <Select label="Segmento" options={INDUSTRIES} value={form.industry} onChange={set("industry")} />
                <Select label="Porte" options={COMPANY_SIZES} value={form.size} onChange={set("size")} />
                <Input label="Funcionários" type="number" min="0" value={form.employees} onChange={set("employees")} />
                <Input label="Faturamento anual (R$)" type="number" min="0" value={form.annualRevenue} onChange={set("annualRevenue")} />
                <Input label="Site" value={form.website} onChange={set("website")} placeholder="empresa.com.br" />
                <Input label="Telefone" value={form.phone} onChange={set("phone")} />
                <Input label="Cidade" value={form.city} onChange={set("city")} />
                <Select label="UF" options={STATES} value={form.state} onChange={set("state")} />
                <Select
                    className="sm:col-span-2"
                    label="Vendedor responsável"
                    placeholder="Sem responsável"
                    options={activeSalesReps.map((rep) => ({ value: rep.id, label: rep.name }))}
                    value={form.ownerId}
                    onChange={set("ownerId")}
                />
                <Textarea className="sm:col-span-2" label="Observações" value={form.notes} onChange={set("notes")} />
            </div>
        </Modal>
    );
}
