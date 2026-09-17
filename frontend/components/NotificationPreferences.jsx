"use client";

import { useEffect, useState } from "react";
import { getNotificationPreferences, updateNotificationPreferences } from "@/lib/api";

const CHANNELS = [
    { key: "emailEnabled", label: "E-mail" },
    { key: "smsEnabled", label: "SMS" },
    { key: "inAppEnabled", label: "No aplicativo" },
];

export default function NotificationPreferences({ customer, onClose }) {
    const [prefs, setPrefs] = useState(null);
    const [error, setError] = useState(null);
    const [saving, setSaving] = useState(false);
    const [saved, setSaved] = useState(false);

    useEffect(() => {
        setPrefs(null);
        setError(null);
        setSaved(false);
        getNotificationPreferences(customer.id)
            .then(setPrefs)
            .catch((err) => setError(err.message));
    }, [customer.id]);

    const toggle = (key) => setPrefs((prev) => ({ ...prev, [key]: !prev[key] }));

    const handleSave = async () => {
        setSaving(true);
        setError(null);
        try {
            const updated = await updateNotificationPreferences(customer.id, {
                emailEnabled: prefs.emailEnabled,
                smsEnabled: prefs.smsEnabled,
                inAppEnabled: prefs.inAppEnabled,
            });
            setPrefs(updated);
            setSaved(true);
        } catch (err) {
            setError(err.message);
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="card preferences-card">
            <div className="actions-row" style={{ justifyContent: "space-between", marginBottom: 12 }}>
                <h2 style={{ margin: 0 }}>Preferencias de notificacao · {customer.name}</h2>
                <button type="button" className="secondary" onClick={onClose}>fechar</button>
            </div>
            <p className="page-subtitle">
                Armazenadas no microsservico de notificacoes. Canais desabilitados fazem a notificacao ser ignorada (status SKIPPED).
            </p>

            {error && <div className="error-banner">{error}</div>}

            {!prefs ? (
                !error && <p>Carregando...</p>
            ) : (
                <>
                    <div className="form-row">
                        {CHANNELS.map((c) => (
                            <label key={c.key} className="checkbox-field">
                                <input type="checkbox" checked={prefs[c.key]} onChange={() => toggle(c.key)} />
                                {c.label}
                            </label>
                        ))}
                    </div>
                    <div className="actions-row" style={{ marginTop: 12 }}>
                        <button type="button" onClick={handleSave} disabled={saving}>
                            {saving ? "Salvando..." : "Salvar preferencias"}
                        </button>
                        <span className="interaction-meta">
                            {prefs.persisted ? `ultima atualizacao: ${prefs.updatedAt ? new Date(prefs.updatedAt).toLocaleString("pt-BR") : "-"}` : "usando padrao (todos os canais habilitados)"}
                            {saved && " · salvo!"}
                        </span>
                    </div>
                </>
            )}
        </div>
    );
}
