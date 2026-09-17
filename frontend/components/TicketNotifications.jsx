"use client";

import { useEffect, useState } from "react";
import { getTicketNotifications, sendTicketNotification } from "@/lib/api";
import Badge from "@/components/Badge";

const CHANNEL_OPTIONS = ["EMAIL", "SMS", "IN_APP"];
const EMPTY_FORM = { channel: "EMAIL", subject: "", message: "" };

const formatDate = (value) => (value ? new Date(value).toLocaleString("pt-BR") : "-");

export default function TicketNotifications({ ticketId, refreshKey = 0, disabled = false }) {
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [form, setForm] = useState(EMPTY_FORM);
    const [submitting, setSubmitting] = useState(false);

    const load = () => {
        setLoading(true);
        getTicketNotifications(ticketId)
            .then((list) => {
                setNotifications(list);
                setError(null);
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    };

    useEffect(load, [ticketId, refreshKey]);

    const handleChange = (field) => (e) => setForm((prev) => ({ ...prev, [field]: e.target.value }));

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            await sendTicketNotification(ticketId, form);
            setForm(EMPTY_FORM);
            load();
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="card">
            <div className="actions-row" style={{ justifyContent: "space-between", marginBottom: 12 }}>
                <h2 style={{ margin: 0 }}>Notificacoes ao cliente (microsservico)</h2>
                <button type="button" className="secondary" onClick={load}>atualizar</button>
            </div>

            {error && <div className="error-banner">{error}</div>}

            {loading ? (
                <p>Carregando...</p>
            ) : notifications.length === 0 ? (
                <p className="empty-state">Nenhuma notificacao registrada para este ticket.</p>
            ) : (
                <table>
                    <thead>
                        <tr>
                            <th>Tipo</th>
                            <th>Canal</th>
                            <th>Status</th>
                            <th>Assunto</th>
                            <th>Solicitado por</th>
                            <th>Criada em</th>
                            <th>Enviada em</th>
                        </tr>
                    </thead>
                    <tbody>
                        {notifications.map((n) => (
                            <tr key={n.id}>
                                <td>{n.type.replaceAll("_", " ")}</td>
                                <td><Badge value={n.channel} /></td>
                                <td>
                                    <Badge value={n.status} />
                                    {n.failureReason && <div className="interaction-meta">{n.failureReason}</div>}
                                </td>
                                <td>{n.subject}</td>
                                <td>{n.requestedBy || "-"}</td>
                                <td>{formatDate(n.createdAt)}</td>
                                <td>{formatDate(n.sentAt)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}

            {disabled ? (
                <p className="page-subtitle" style={{ marginTop: 16 }}>Ticket fechado: nao envia novas notificacoes manuais.</p>
            ) : (
                <form onSubmit={handleSubmit} style={{ marginTop: 16 }}>
                    <div className="form-row">
                        <div className="form-field">
                            <label>Canal</label>
                            <select value={form.channel} onChange={handleChange("channel")}>
                                {CHANNEL_OPTIONS.map((c) => (
                                    <option key={c} value={c}>{c}</option>
                                ))}
                            </select>
                        </div>
                        <div className="form-field" style={{ flex: 3 }}>
                            <label>Assunto</label>
                            <input required maxLength={160} value={form.subject} onChange={handleChange("subject")} />
                        </div>
                    </div>
                    <div className="form-field">
                        <label>Mensagem</label>
                        <textarea required maxLength={2000} value={form.message} onChange={handleChange("message")} />
                    </div>
                    <button type="submit" disabled={submitting}>
                        {submitting ? "Enviando..." : "Enviar notificacao manual"}
                    </button>
                </form>
            )}
        </div>
    );
}
