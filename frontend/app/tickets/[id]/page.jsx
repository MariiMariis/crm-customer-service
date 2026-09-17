"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import {
    getTicket,
    changeTicketStatus,
    addInteraction,
    getTicketStatusHistory,
    getTicketRevisions,
} from "@/lib/api";
import Badge from "@/components/Badge";

const STATUS_OPTIONS = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"];

const formatDate = (value) => (value ? new Date(value).toLocaleString("pt-BR") : "-");

export default function TicketDetailPage() {
    const { id } = useParams();

    const [ticket, setTicket] = useState(null);
    const [statusHistory, setStatusHistory] = useState([]);
    const [revisions, setRevisions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [updatingStatus, setUpdatingStatus] = useState(false);
    const [statusReason, setStatusReason] = useState("");
    const [interactionForm, setInteractionForm] = useState({ author: "", message: "" });
    const [submittingInteraction, setSubmittingInteraction] = useState(false);

    const loadHistory = () => {
        Promise.all([getTicketStatusHistory(id), getTicketRevisions(id)])
            .then(([history, revs]) => {
                setStatusHistory(history);
                setRevisions(revs);
            })
            .catch((err) => setError(err.message));
    };

    const loadTicket = () => {
        setLoading(true);
        getTicket(id)
            .then((t) => {
                setTicket(t);
                loadHistory();
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    };

    useEffect(loadTicket, [id]);

    const handleStatusChange = async (e) => {
        const newStatus = e.target.value;
        setUpdatingStatus(true);
        setError(null);
        try {
            const updated = await changeTicketStatus(id, newStatus, statusReason || undefined);
            setTicket(updated);
            setStatusReason("");
            loadHistory();
        } catch (err) {
            setError(err.message);
        } finally {
            setUpdatingStatus(false);
        }
    };

    const handleInteractionSubmit = async (e) => {
        e.preventDefault();
        setSubmittingInteraction(true);
        setError(null);
        try {
            await addInteraction(id, interactionForm);
            setInteractionForm({ author: "", message: "" });
            loadTicket();
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmittingInteraction(false);
        }
    };

    if (loading) return <p>Carregando...</p>;
    if (error && !ticket) return <div className="error-banner">{error}</div>;
    if (!ticket) return null;

    const isClosed = ticket.status === "CLOSED";

    return (
        <>
            <Link className="link" href="/tickets">&larr; voltar para tickets</Link>
            <h1 style={{ marginTop: 12 }}>{ticket.subject}</h1>
            <p className="page-subtitle">
                Cliente: {ticket.customerName || `#${ticket.customerId}`} · Atendente: {ticket.agentName || "Nao atribuido"}
            </p>

            {error && <div className="error-banner">{error}</div>}

            <div className="card">
                <h2>Detalhes</h2>
                <p>{ticket.description}</p>
                <div className="form-row">
                    <div className="form-field">
                        <label>Prioridade</label>
                        <div><Badge value={ticket.priority} /></div>
                    </div>
                    <div className="form-field">
                        <label>Status</label>
                        <select value={ticket.status} onChange={handleStatusChange} disabled={updatingStatus || isClosed}>
                            {STATUS_OPTIONS.map((s) => (
                                <option key={s} value={s}>{s}</option>
                            ))}
                        </select>
                    </div>
                    <div className="form-field">
                        <label>Motivo da mudanca (opcional)</label>
                        <input
                            value={statusReason}
                            disabled={isClosed}
                            placeholder="ex.: cliente confirmou a solucao"
                            onChange={(e) => setStatusReason(e.target.value)}
                        />
                    </div>
                </div>
                <div className="form-row">
                    <div className="form-field">
                        <label>Criado em</label>
                        <div>{formatDate(ticket.createdAt)} por {ticket.createdBy || "-"}</div>
                    </div>
                    <div className="form-field">
                        <label>Ultima alteracao</label>
                        <div>{formatDate(ticket.updatedAt)} por {ticket.updatedBy || "-"} (versao {ticket.version})</div>
                    </div>
                    <div className="form-field">
                        <label>Resolvido / Fechado em</label>
                        <div>{formatDate(ticket.resolvedAt)} / {formatDate(ticket.closedAt)}</div>
                    </div>
                </div>
            </div>

            <div className="card">
                <h2>Historico de status</h2>
                {statusHistory.length === 0 ? (
                    <p className="empty-state">Nenhuma mudanca de status registrada.</p>
                ) : (
                    <table>
                        <thead>
                            <tr>
                                <th>De</th>
                                <th>Para</th>
                                <th>Motivo</th>
                                <th>Por</th>
                                <th>Quando</th>
                            </tr>
                        </thead>
                        <tbody>
                            {statusHistory.map((h) => (
                                <tr key={h.id}>
                                    <td>{h.fromStatus ? <Badge value={h.fromStatus} /> : "-"}</td>
                                    <td><Badge value={h.toStatus} /></td>
                                    <td>{h.reason || "-"}</td>
                                    <td>{h.changedBy || "-"}</td>
                                    <td>{formatDate(h.changedAt)}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

            <div className="card">
                <h2>Historico de interacoes</h2>
                {ticket.interactions.length === 0 ? (
                    <p className="empty-state">Nenhuma interacao registrada ainda.</p>
                ) : (
                    ticket.interactions.map((i) => (
                        <div key={i.id} className="interaction">
                            <div className="interaction-meta">
                                {i.author} · {formatDate(i.createdAt)}
                            </div>
                            <div>{i.message}</div>
                        </div>
                    ))
                )}

                {isClosed ? (
                    <p className="page-subtitle" style={{ marginTop: 16 }}>Ticket fechado: nao aceita novas interacoes.</p>
                ) : (
                    <form onSubmit={handleInteractionSubmit} style={{ marginTop: 16 }}>
                        <div className="form-row">
                            <div className="form-field">
                                <label>Autor</label>
                                <input
                                    required
                                    value={interactionForm.author}
                                    onChange={(e) => setInteractionForm((p) => ({ ...p, author: e.target.value }))}
                                />
                            </div>
                        </div>
                        <div className="form-field">
                            <label>Mensagem</label>
                            <textarea
                                required
                                value={interactionForm.message}
                                onChange={(e) => setInteractionForm((p) => ({ ...p, message: e.target.value }))}
                            />
                        </div>
                        <button type="submit" disabled={submittingInteraction}>
                            {submittingInteraction ? "Enviando..." : "Adicionar interacao"}
                        </button>
                    </form>
                )}
            </div>

            <div className="card">
                <h2>Revisoes (auditoria Envers)</h2>
                {revisions.length === 0 ? (
                    <p className="empty-state">Nenhuma revisao encontrada.</p>
                ) : (
                    <table>
                        <thead>
                            <tr>
                                <th>Rev</th>
                                <th>Tipo</th>
                                <th>Ator</th>
                                <th>Quando</th>
                                <th>Status</th>
                                <th>Prioridade</th>
                                <th>Atendente</th>
                                <th>Assunto</th>
                            </tr>
                        </thead>
                        <tbody>
                            {revisions.map((r) => (
                                <tr key={r.revision}>
                                    <td>{r.revision}</td>
                                    <td>{r.type}</td>
                                    <td>{r.actor}</td>
                                    <td>{formatDate(r.timestamp)}</td>
                                    <td><Badge value={r.data?.status} /></td>
                                    <td><Badge value={r.data?.priority} /></td>
                                    <td>{r.data?.agentName || "-"}</td>
                                    <td>{r.data?.subject}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </>
    );
}
