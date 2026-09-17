"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getTickets, createTicket, getCustomers, getAgents } from "@/lib/api";
import Badge from "@/components/Badge";

const EMPTY_FORM = { subject: "", description: "", priority: "MEDIUM", customerId: "", agentId: "" };
const STATUS_OPTIONS = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"];
const PRIORITY_OPTIONS = ["LOW", "MEDIUM", "HIGH", "URGENT"];

export default function TicketsPage() {
    const [tickets, setTickets] = useState([]);
    const [customers, setCustomers] = useState([]);
    const [agents, setAgents] = useState([]);
    const [statusFilter, setStatusFilter] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [form, setForm] = useState(EMPTY_FORM);
    const [submitting, setSubmitting] = useState(false);

    const loadTickets = (status) => {
        setLoading(true);
        getTickets(status || undefined)
            .then(setTickets)
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        loadTickets(statusFilter);
    }, [statusFilter]);

    useEffect(() => {
        Promise.all([getCustomers(), getAgents()])
            .then(([c, a]) => {
                setCustomers(c);
                setAgents(a);
            })
            .catch((err) => setError(err.message));
    }, []);

    const customerName = (id) => customers.find((c) => c.id === id)?.name || `#${id}`;
    const agentName = (id) => (id ? agents.find((a) => a.id === id)?.name || `#${id}` : "-");

    const handleChange = (field) => (e) =>
        setForm((prev) => ({ ...prev, [field]: e.target.value }));

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            await createTicket({
                ...form,
                customerId: Number(form.customerId),
                agentId: form.agentId ? Number(form.agentId) : null,
            });
            setForm(EMPTY_FORM);
            loadTickets(statusFilter);
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <>
            <h1>Tickets</h1>
            <p className="page-subtitle">Chamados de atendimento abertos pelos clientes</p>

            {error && <div className="error-banner">{error}</div>}

            <div className="card">
                <h2>Novo ticket</h2>
                <form onSubmit={handleSubmit}>
                    <div className="form-field">
                        <label>Assunto</label>
                        <input required value={form.subject} onChange={handleChange("subject")} />
                    </div>
                    <div className="form-field">
                        <label>Descricao</label>
                        <textarea required value={form.description} onChange={handleChange("description")} />
                    </div>
                    <div className="form-row">
                        <div className="form-field">
                            <label>Cliente</label>
                            <select required value={form.customerId} onChange={handleChange("customerId")}>
                                <option value="">Selecione...</option>
                                {customers.map((c) => (
                                    <option key={c.id} value={c.id}>{c.name}</option>
                                ))}
                            </select>
                        </div>
                        <div className="form-field">
                            <label>Atendente (opcional)</label>
                            <select value={form.agentId} onChange={handleChange("agentId")}>
                                <option value="">Nao atribuido</option>
                                {agents.map((a) => (
                                    <option key={a.id} value={a.id}>{a.name}</option>
                                ))}
                            </select>
                        </div>
                        <div className="form-field">
                            <label>Prioridade</label>
                            <select value={form.priority} onChange={handleChange("priority")}>
                                {PRIORITY_OPTIONS.map((p) => (
                                    <option key={p} value={p}>{p}</option>
                                ))}
                            </select>
                        </div>
                    </div>
                    <button type="submit" disabled={submitting || customers.length === 0}>
                        {submitting ? "Salvando..." : "Abrir ticket"}
                    </button>
                    {customers.length === 0 && (
                        <p className="page-subtitle">Cadastre ao menos um cliente antes de abrir um ticket.</p>
                    )}
                </form>
            </div>

            <div className="card">
                <div className="actions-row" style={{ justifyContent: "space-between", marginBottom: 12 }}>
                    <h2 style={{ margin: 0 }}>Lista de tickets</h2>
                    <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
                        <option value="">Todos os status</option>
                        {STATUS_OPTIONS.map((s) => (
                            <option key={s} value={s}>{s}</option>
                        ))}
                    </select>
                </div>

                {loading ? (
                    <p>Carregando...</p>
                ) : tickets.length === 0 ? (
                    <p className="empty-state">Nenhum ticket encontrado.</p>
                ) : (
                    <table>
                        <thead>
                            <tr>
                                <th>Assunto</th>
                                <th>Cliente</th>
                                <th>Atendente</th>
                                <th>Status</th>
                                <th>Prioridade</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {tickets.map((t) => (
                                <tr key={t.id}>
                                    <td>{t.subject}</td>
                                    <td>{customerName(t.customerId)}</td>
                                    <td>{agentName(t.agentId)}</td>
                                    <td><Badge value={t.status} /></td>
                                    <td><Badge value={t.priority} /></td>
                                    <td><Link className="link" href={`/tickets/${t.id}`}>ver detalhes</Link></td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </>
    );
}
