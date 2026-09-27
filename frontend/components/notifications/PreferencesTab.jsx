"use client";

import { useState } from "react";
import { Bell, Mail } from "lucide-react";
import { api } from "@/lib/api";
import { useApi } from "@/lib/useApi";
import { dateTime } from "@/lib/format";
import { useToast } from "@/components/providers/ToastProvider";
import { ErrorBanner, Spinner } from "@/components/ui/Feedback";
import { Section } from "@/components/ui/Layout";

function Toggle({ checked, disabled, onChange, label }) {
    return (
        <button
            role="switch"
            aria-checked={checked}
            aria-label={label}
            disabled={disabled}
            onClick={() => onChange(!checked)}
            className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition disabled:cursor-not-allowed disabled:opacity-50 ${checked ? "bg-brand-500" : "bg-surface-600"}`}
        >
            <span className={`inline-block h-5 w-5 transform rounded-full bg-white shadow transition ${checked ? "translate-x-5" : "translate-x-0.5"}`} />
        </button>
    );
}

function ChannelRow({ icon: Icon, title, description, children }) {
    return (
        <div className="flex items-start gap-4 py-4 first:pt-0 last:pb-0">
            <div className="rounded-lg bg-surface-800 p-2.5 text-slate-300">
                <Icon className="h-5 w-5" />
            </div>
            <div className="flex-1">
                <p className="font-medium text-white">{title}</p>
                <p className="mt-0.5 text-sm text-slate-400">{description}</p>
            </div>
            {children}
        </div>
    );
}

export default function PreferencesTab({ user }) {
    const toast = useToast();
    const [saving, setSaving] = useState(false);
    const { data, loading, error, reload, setData } = useApi(() => api.notifications.preferences(user.id), [user.id]);

    const toggleEmail = async (emailEnabled) => {
        setSaving(true);
        try {
            setData(await api.notifications.updatePreferences(user.id, emailEnabled));
            toast.success(emailEnabled ? "E-mails ativados" : "E-mails desativados: novos alertas ficarão só na caixa de entrada");
        } catch (err) {
            toast.error(err);
        } finally {
            setSaving(false);
        }
    };

    return (
        <Section title={`Preferências de ${user.name}`} className="max-w-3xl">
            <ErrorBanner error={error} onRetry={reload} />
            {loading && !data ? (
                <Spinner />
            ) : (
                <div className="divide-y divide-surface-700/60">
                    <ChannelRow icon={Bell} title="Caixa de entrada" description="Sempre ativa. Todo alerta aparece no sino do topo e nesta página.">
                        <Toggle checked disabled label="Caixa de entrada" onChange={() => null} />
                    </ChannelRow>
                    <ChannelRow
                        icon={Mail}
                        title="E-mail"
                        description={`Envio para ${user.email ?? "o e-mail cadastrado"}. Quando desligado, o e-mail é registrado como não enviado.`}
                    >
                        <Toggle checked={Boolean(data?.emailEnabled)} disabled={saving} label="E-mail" onChange={toggleEmail} />
                    </ChannelRow>
                    <p className="pt-4 text-xs text-slate-500">
                        {data?.persisted ? `Última alteração em ${dateTime(data.updatedAt)}` : "Usando o padrão da plataforma (e-mail ativado)."}
                    </p>
                </div>
            )}
        </Section>
    );
}
