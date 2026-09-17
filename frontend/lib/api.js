const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

async function request(path, options = {}) {
    const res = await fetch(`${API_URL}${path}`, {
        headers: { "Content-Type": "application/json" },
        cache: "no-store",
        ...options,
    });

    if (!res.ok) {
        let message = `Erro ${res.status} ao chamar ${path}`;
        try {
            const body = await res.json();
            message = body.message || message;
        } catch {
            // resposta sem corpo JSON (ex.: 204)
        }
        throw new Error(message);
    }

    if (res.status === 204) return null;
    return res.json();
}

// ---- Customers ----
export const getCustomers = () => request("/customers");
export const getCustomer = (id) => request(`/customers/${id}`);
export const createCustomer = (data) =>
    request("/customers", { method: "POST", body: JSON.stringify(data) });
export const deleteCustomer = (id) =>
    request(`/customers/${id}`, { method: "DELETE" });

// ---- Agents ----
export const getAgents = () => request("/agents");
export const createAgent = (data) =>
    request("/agents", { method: "POST", body: JSON.stringify(data) });
export const deleteAgent = (id) =>
    request(`/agents/${id}`, { method: "DELETE" });

// ---- Tickets ----
export const getTickets = (status) =>
    request(status ? `/tickets?status=${status}` : "/tickets");
export const getTicket = (id) => request(`/tickets/${id}`);
export const createTicket = (data) =>
    request("/tickets", { method: "POST", body: JSON.stringify(data) });
export const changeTicketStatus = (id, status) =>
    request(`/tickets/${id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status }),
    });
export const addInteraction = (ticketId, data) =>
    request(`/tickets/${ticketId}/interactions`, {
        method: "POST",
        body: JSON.stringify(data),
    });
