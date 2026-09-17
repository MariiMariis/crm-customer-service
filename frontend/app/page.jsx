"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getCustomers, getAgents, getTickets } from "@/lib/api";
import Badge from "@/components/Badge";

export default function DashboardPage() {
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [customers, setCustomers] = useState([]);
    const [agents, setAgents] = useState([]);
    const [tickets, setTickets] = useState([]);

    useEffect(() => {
        Promise.all([getCustomers(), getAgents(), getTickets()])
            .then(([c, a, t]) => {
                setCustomers(c);
                setAgents(a);
                setTickets(t);
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    }, []);

    const openCount = tickets.filter((t) => t.status === "OPEN").length;
    const inProgressCount = tickets.filter((t) => t.status === "IN_PROGRESS").length;
    const recentTickets = [...tickets]
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 5);

    return (
        <>
            <h1>Dashboard</h1>
            <p className="page-subtitle">Visao geral do atendimento ao cliente</p>

            {error && <div className="error-banner">Nao foi possivel carregar os dados: {error}. Verifique se o back-end esta rodando em http://localhost:8080.</div>}

            {loading ? (
                <p>Carregando...</p>
            ) : (
                <>
                    <div className="stats-grid">
                        <div className="stat-card">
                            <div className="stat-value">{customers.length}</div>
                            <div className="stat-label">Clientes</div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-value">{agents.length}</div>
                            <div className="stat-label">Atendentes</div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-value">{tickets.length}</div>
                            <div className="stat-label">Tickets totais</div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-value">{openCount}</div>
                            <div className="stat-label">Tickets abertos</div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-value">{inProgressCount}</div>
                            <div className="stat-label">Em andamento</div>
                        </div>
                    </div>

                    <div className="card">
                        <h2>Tickets recentes</h2>
                        {recentTickets.length === 0 ? (
                            <p className="empty-state">Nenhum ticket cadastrado ainda.</p>
                        ) : (
                            <table>
                                <thead>
                                    <tr>
                                        <th>Assunto</th>
                                        <th>Status</th>
                                        <th>Prioridade</th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {recentTickets.map((t) => (
                                        <tr key={t.id}>
                                            <td>{t.subject}</td>
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
            )}
        </>
    );
}
