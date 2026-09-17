const NOTIFICATION_API_URL =
    process.env.NEXT_PUBLIC_NOTIFICATION_API_URL || "http://localhost:8081/api";

async function request(path, options = {}) {
    const res = await fetch(`${NOTIFICATION_API_URL}${path}`, {
        headers: { "Content-Type": "application/json" },
        cache: "no-store",
        ...options,
    });

    if (!res.ok) {
        let message = `Erro ${res.status} ao chamar o servico de notificacoes (${path})`;
        try {
            const body = await res.json();
            message = body.message || message;
        } catch {
            message = `Erro ${res.status} ao chamar o servico de notificacoes (${path})`;
        }
        throw new Error(message);
    }

    if (res.status === 204) return null;
    return res.json();
}

const toQuery = (filters = {}) => {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== "") params.set(key, value);
    });
    const query = params.toString();
    return query ? `?${query}` : "";
};

export const NOTIFICATION_SERVICE_URL = NOTIFICATION_API_URL;

export const getNotifications = (filters) => request(`/notifications${toQuery(filters)}`);
export const getNotification = (id) => request(`/notifications/${id}`);
export const getNotificationStats = () => request("/notifications/stats");
export const dispatchNotification = (id) =>
    request(`/notifications/${id}/dispatch`, { method: "POST" });
export const dispatchPendingNotifications = () =>
    request("/notifications/dispatch", { method: "POST" });
export const markNotificationRead = (id) =>
    request(`/notifications/${id}/read`, { method: "PATCH" });
export const deleteNotification = (id) =>
    request(`/notifications/${id}`, { method: "DELETE" });
