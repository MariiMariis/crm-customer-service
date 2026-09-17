"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { getTicket, changeTicketStatus, addInteraction, getCustomers, getAgents } from "@/lib/api";
import Badge from "@/components/Badge";

const STATUS_OPTIONS = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"];

export default function TicketDetailPage() {
    const { id } = useParams();
    const router = useRouter();

    const [ticket, setTicket] = useState(null);
    const [customers, setCustomers] = useState([]);
    const [agents, setAgents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [updatingStatus, setUpdatingStatus] = useState(false);
    const [interactionForm, setInteractionForm] = useState({ author: "", message: "" });
    const [submittingInteraction, setSubmittingInteraction] = useState(false);

    const loadTicket = () => {
        setLoading(true);
        getTicket(id)
            .then(setTicket)
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    };

    useEffect(loadTicket, [id]);

    useEffect(() => {
        Promise.all([getCustomers(), getAgents()])
            .then(([c, a]) => {
                setCustomers(c);
                setAgents(a);
            })
            .catch(() => {});
    }, []);

    const customerName = (cid) => customers.find((c) => c.id === cid)?.name || `#${cid}`;
    const agentName = (aid) => (aid ? agents.find((a) => a.id === aid)?.name || `#${aid}` : "Nao atribuido");

    const handleStatusChange = async (e) => {
        const newStatus = e.target.value;
        setUpdatingStatus(true);
        setError(null);
        try {
            const updated = await changeTicketStatus(id, newStatus);
            setTicket(updated);
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

    return (
        <>
            <Link className="link" href="/tickets">&larr; voltar para tickets</Link>
            <h1 style={{ marginTop: 12 }}>{ticket.subject}</h1>
            <p className="page-subtitle">
                Cliente: {customerName(ticket.customerId)} · Atendente: {agentName(ticket.agentId)}
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
                        <select value={ticket.status} onChange={handleStatusChange} disabled={updatingStatus}>
                            {STATUS_OPTIONS.map((s) => (
                                <option key={s} value={s}>{s}</option>
                            ))}
                        </select>
                    </div>
                </div>
            </div>

            <div className="card">
                <h2>Historico de interacoes</h2>
                {ticket.interactions.length === 0 ? (
                    <p className="empty-state">Nenhuma interacao registrada ainda.</p>
                ) : (
                    ticket.interactions.map((i) => (
                        <div key={i.id} className="interaction">
                            <div className="interaction-meta">
                                {i.author} · {new Date(i.createdAt).toLocaleString("pt-BR")}
                            </div>
                            <div>{i.message}</div>
                        </div>
                    ))
                )}

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
            </div>
        </>
    );
}
