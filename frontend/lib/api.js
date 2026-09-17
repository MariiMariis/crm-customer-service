const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";
const ACTOR = process.env.NEXT_PUBLIC_ACTOR || "web-frontend";

async function request(path, options = {}) {
    const res = await fetch(`${API_URL}${path}`, {
        headers: { "Content-Type": "application/json", "X-Actor": ACTOR },
        cache: "no-store",
        ...options,
    });

    if (!res.ok) {
        let message = `Erro ${res.status} ao chamar ${path}`;
        try {
            const body = await res.json();
            message = body.message || message;
        } catch {
            message = `Erro ${res.status} ao chamar ${path}`;
        }
        throw new Error(message);
    }

    if (res.status === 204) return null;
    return res.json();
}

export const getCustomers = () => request("/customers");
export const getCustomer = (id) => request(`/customers/${id}`);
export const createCustomer = (data) =>
    request("/customers", { method: "POST", body: JSON.stringify(data) });
export const deleteCustomer = (id) =>
    request(`/customers/${id}`, { method: "DELETE" });
export const getCustomerRevisions = (id) => request(`/customers/${id}/revisions`);

export const getAgents = () => request("/agents");
export const createAgent = (data) =>
    request("/agents", { method: "POST", body: JSON.stringify(data) });
export const deleteAgent = (id) =>
    request(`/agents/${id}`, { method: "DELETE" });
export const getAgentRevisions = (id) => request(`/agents/${id}/revisions`);

export const getTickets = (status) =>
    request(status ? `/tickets?status=${status}` : "/tickets");
export const getTicket = (id) => request(`/tickets/${id}`);
export const getTicketStats = () => request("/tickets/stats");
export const createTicket = (data) =>
    request("/tickets", { method: "POST", body: JSON.stringify(data) });
export const changeTicketStatus = (id, status, reason) =>
    request(`/tickets/${id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status, reason }),
    });
export const addInteraction = (ticketId, data) =>
    request(`/tickets/${ticketId}/interactions`, {
        method: "POST",
        body: JSON.stringify(data),
    });
export const getTicketStatusHistory = (id) => request(`/tickets/${id}/status-history`);
export const getTicketRevisions = (id) => request(`/tickets/${id}/revisions`);

export const getTicketNotifications = (id) => request(`/tickets/${id}/notifications`);
export const sendTicketNotification = (id, data) =>
    request(`/tickets/${id}/notifications`, { method: "POST", body: JSON.stringify(data) });
export const getNotificationPreferences = (customerId) =>
    request(`/customers/${customerId}/notification-preferences`);
export const updateNotificationPreferences = (customerId, data) =>
    request(`/customers/${customerId}/notification-preferences`, {
        method: "PUT",
        body: JSON.stringify(data),
    });
export const getNotificationServiceStatus = () => request("/notifications/status");
