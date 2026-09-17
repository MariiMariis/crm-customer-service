"use client";

import { useEffect, useState } from "react";
import { getNotificationServiceStatus } from "@/lib/api";

export default function ServiceStatus({ compact = false }) {
    const [status, setStatus] = useState(null);
    const [error, setError] = useState(null);

    const load = () => {
        getNotificationServiceStatus()
            .then((s) => {
                setStatus(s);
                setError(null);
            })
            .catch((err) => setError(err.message));
    };

    useEffect(() => {
        load();
        const timer = setInterval(load, 15000);
        return () => clearInterval(timer);
    }, []);

    if (error) {
        return <div className="service-status service-status-down">Monolito indisponivel: {error}</div>;
    }
    if (!status) {
        return <div className="service-status">Consultando o servico de notificacoes...</div>;
    }

    const cssClass = !status.enabled
        ? "service-status-disabled"
        : status.available
            ? "service-status-up"
            : "service-status-down";
    const label = !status.enabled
        ? "Integracao desabilitada"
        : status.available
            ? "Microsservico de notificacoes online"
            : "Microsservico de notificacoes offline";

    return (
        <div className={`service-status ${cssClass}`}>
            <span className="service-status-dot" />
            <strong>{label}</strong>
            {!compact && (
                <span className="service-status-meta">
                    health: {status.health} · circuit breaker: {status.circuitState} · instancia: {status.instances?.join(", ") || "-"}
                </span>
            )}
            <button type="button" className="secondary service-status-refresh" onClick={load}>
                atualizar
            </button>
        </div>
    );
}
