"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
    getNotifications,
    getNotificationStats,
    dispatchNotification,
    dispatchPendingNotifications,
    markNotificationRead,
    deleteNotification,
    NOTIFICATION_SERVICE_URL,
} from "@/lib/notificationApi";
import Badge from "@/components/Badge";
import ServiceStatus from "@/components/ServiceStatus";

const STATUS_OPTIONS = ["PENDING", "SENT", "FAILED", "SKIPPED"];
const CHANNEL_OPTIONS = ["EMAIL", "SMS", "IN_APP"];
const TYPE_OPTIONS = ["TICKET_CREATED", "TICKET_STATUS_CHANGED", "TICKET_INTERACTION_ADDED", "MANUAL"];
const EMPTY_FILTERS = { status: "", channel: "", type: "", ticketId: "" };

const formatDate = (value) => (value ? new Date(value).toLocaleString("pt-BR") : "-");

export default function NotificationsPage() {
    const [notifications, setNotifications] = useState([]);
    const [stats, setStats] = useState(null);
    const [filters, setFilters] = useState(EMPTY_FILTERS);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [working, setWorking] = useState(null);

    const load = () => {
        setLoading(true);
        Promise.all([getNotifications(filters), getNotificationStats()])
            .then(([list, s]) => {
                setNotifications(list);
                setStats(s);
                setError(null);
            })
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    };

    useEffect(load, [filters]);

    const run = async (id, action) => {
        setWorking(id);
        setError(null);
        try {
            await action();
            load();
        } catch (err) {
            setError(err.message);
        } finally {
            setWorking(null);
        }
    };

    const handleFilter = (field) => (e) => setFilters((prev) => ({ ...prev, [field]: e.target.value }));

    return (
        <>
            <h1>Notificacoes</h1>
            <p className="page-subtitle">
                Central de notificacoes ao cliente, consumida diretamente do microsservico em <code>{NOTIFICATION_SERVICE_URL}</code>
            </p>

            <ServiceStatus />

            {error && <div className="error-banner">{error}</div>}

            {stats && (
                <div className="stats-grid">
                    <div className="stat-card">
                        <div className="stat-value">{stats.total}</div>
                        <div className="stat-label">Total</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value">{stats.byStatus.PENDING}</div>
                        <div className="stat-label">Pendentes</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value">{stats.byStatus.SENT}</div>
                        <div className="stat-label">Enviadas</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value">{stats.byStatus.FAILED}</div>
                        <div className="stat-label">Falhas</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value">{stats.byStatus.SKIPPED}</div>
                        <div className="stat-label">Ignoradas</div>
                    </div>
                    <div className="stat-card">
                        <div className="stat-value">{stats.unread}</div>
                        <div className="stat-label">Nao lidas</div>
                    </div>
                </div>
            )}

            <div className="card">
                <div className="actions-row" style={{ justifyContent: "space-between", marginBottom: 12, flexWrap: "wrap" }}>
                    <h2 style={{ margin: 0 }}>Fila de notificacoes</h2>
                    <div className="actions-row" style={{ flexWrap: "wrap" }}>
                        <select value={filters.status} onChange={handleFilter("status")}>
                            <option value="">Todos os status</option>
                            {STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s}</option>)}
                        </select>
                        <select value={filters.channel} onChange={handleFilter("channel")}>
                            <option value="">Todos os canais</option>
                            {CHANNEL_OPTIONS.map((c) => <option key={c} value={c}>{c}</option>)}
                        </select>
                        <select value={filters.type} onChange={handleFilter("type")}>
                            <option value="">Todos os tipos</option>
                            {TYPE_OPTIONS.map((t) => <option key={t} value={t}>{t}</option>)}
                        </select>
                        <input
                            type="number"
                            min="1"
                            placeholder="ticket #"
                            style={{ width: 110 }}
                            value={filters.ticketId}
                            onChange={handleFilter("ticketId")}
                        />
                        <button type="button" className="secondary" onClick={() => setFilters(EMPTY_FILTERS)}>limpar</button>
                        <button
                            type="button"
                            disabled={working === "all"}
                            onClick={() => run("all", dispatchPendingNotifications)}
                        >
                            {working === "all" ? "Processando..." : "Processar pendentes"}
                        </button>
                    </div>
                </div>

                {loading ? (
                    <p>Carregando...</p>
                ) : notifications.length === 0 ? (
                    <p className="empty-state">Nenhuma notificacao encontrada. Abra ou movimente um ticket para gerar notificacoes.</p>
                ) : (
                    <table>
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Ticket</th>
                                <th>Destinatario</th>
                                <th>Tipo</th>
                                <th>Canal</th>
                                <th>Status</th>
                                <th>Assunto</th>
                                <th>Criada</th>
                                <th>Enviada</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {notifications.map((n) => (
                                <tr key={n.id}>
                                    <td>{n.id}</td>
                                    <td><Link className="link" href={`/tickets/${n.ticketId}`}>#{n.ticketId}</Link></td>
                                    <td>
                                        {n.recipientName}
                                        <div className="interaction-meta">{n.recipientEmail || n.recipientPhone || "-"}</div>
                                    </td>
                                    <td>{n.type.replaceAll("_", " ")}</td>
                                    <td><Badge value={n.channel} /></td>
                                    <td>
                                        <Badge value={n.status} />
                                        {n.readAt && <div className="interaction-meta">lida em {formatDate(n.readAt)}</div>}
                                        {n.failureReason && <div className="interaction-meta">{n.failureReason} (tentativas: {n.attempts})</div>}
                                    </td>
                                    <td title={n.message}>{n.subject}</td>
                                    <td>{formatDate(n.createdAt)}</td>
                                    <td>{formatDate(n.sentAt)}</td>
                                    <td>
                                        <div className="actions-row">
                                            {(n.status === "PENDING" || n.status === "FAILED") && (
                                                <button
                                                    type="button"
                                                    disabled={working === n.id}
                                                    onClick={() => run(n.id, () => dispatchNotification(n.id))}
                                                >
                                                    enviar
                                                </button>
                                            )}
                                            {n.status === "SENT" && !n.readAt && (
                                                <button
                                                    type="button"
                                                    className="secondary"
                                                    disabled={working === n.id}
                                                    onClick={() => run(n.id, () => markNotificationRead(n.id))}
                                                >
                                                    marcar lida
                                                </button>
                                            )}
                                            <button
                                                type="button"
                                                className="secondary"
                                                disabled={working === n.id}
                                                onClick={() => {
                                                    if (confirm("Excluir esta notificacao?")) run(n.id, () => deleteNotification(n.id));
                                                }}
                                            >
                                                excluir
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </>
    );
}
