const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";
export const CURRENT_USER_KEY = "pbcrm.currentUser";

export class ApiError extends Error {
    constructor(status, message, details = []) {
        super(message);
        this.status = status;
        this.details = details;
    }
}

function currentActor() {
    if (typeof window === "undefined") return "web";
    try {
        const stored = JSON.parse(window.localStorage.getItem(CURRENT_USER_KEY) ?? "null");
        return stored?.name ?? "web";
    } catch {
        return "web";
    }
}

function query(params = {}) {
    const search = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== "") search.set(key, value);
    });
    const text = search.toString();
    return text ? `?${text}` : "";
}

async function request(path, { method = "GET", body } = {}) {
    let response;
    try {
        response = await fetch(`${API_URL}${path}`, {
            method,
            cache: "no-store",
            headers: { "Content-Type": "application/json", "X-Actor": currentActor() },
            body: body === undefined ? undefined : JSON.stringify(body),
        });
    } catch {
        throw new ApiError(0, "Não foi possível conectar ao API Gateway. Verifique se os serviços estão rodando.");
    }
    if (response.status === 204) return null;
    const payload = await response.json().catch(() => null);
    if (!response.ok) {
        throw new ApiError(response.status, payload?.message ?? `Erro ${response.status}`, payload?.details ?? []);
    }
    return payload;
}

const get = (path, params) => request(`${path}${query(params)}`);
const post = (path, body) => request(path, { method: "POST", body: body ?? {} });
const put = (path, body) => request(path, { method: "PUT", body });
const del = (path) => request(path, { method: "DELETE" });

export const api = {
    salesReps: {
        list: (params) => get("/sales-reps", { pageSize: 100, ...params }),
        get: (id) => get(`/sales-reps/${id}`),
    },
    companies: {
        list: (params) => get("/companies", params),
        get: (id) => get(`/companies/${id}`),
        create: (body) => post("/companies", body),
        update: (id, body) => put(`/companies/${id}`, body),
        archive: (id) => post(`/companies/${id}/archive`),
        restore: (id) => post(`/companies/${id}/restore`),
        revisions: (id) => get(`/companies/${id}/revisions`),
    },
    contacts: {
        list: (params) => get("/contacts", params),
        get: (id) => get(`/contacts/${id}`),
        create: (body) => post("/contacts", body),
        update: (id, body) => put(`/contacts/${id}`, body),
        makePrimary: (id) => post(`/contacts/${id}/make-primary`),
        archive: (id) => post(`/contacts/${id}/archive`),
        restore: (id) => post(`/contacts/${id}/restore`),
    },
    products: {
        list: (params) => get("/products", { pageSize: 100, ...params }),
        taxonomy: () => get("/products/taxonomy"),
    },
    leads: {
        list: (params) => get("/leads", params),
        get: (id) => get(`/leads/${id}`),
        stats: () => get("/leads/stats"),
        create: (body) => post("/leads", body),
        update: (id, body) => put(`/leads/${id}`, body),
        assign: (id, ownerId) => post(`/leads/${id}/assign`, { ownerId }),
        contacted: (id) => post(`/leads/${id}/contacted`),
        qualify: (id) => post(`/leads/${id}/qualify`),
        disqualify: (id, reason) => post(`/leads/${id}/disqualify`, { reason }),
        reopen: (id) => post(`/leads/${id}/reopen`),
        convert: (id, body) => post(`/leads/${id}/convert`, body),
        archive: (id) => post(`/leads/${id}/archive`),
        revisions: (id) => get(`/leads/${id}/revisions`),
    },
    opportunities: {
        list: (params) => get("/opportunities", params),
        get: (id) => get(`/opportunities/${id}`),
        pipeline: (ownerId) => get("/opportunities/pipeline", { ownerId }),
        create: (body) => post("/opportunities", body),
        update: (id, body) => put(`/opportunities/${id}`, body),
        addItem: (id, body) => post(`/opportunities/${id}/items`, body),
        changeItem: (id, itemId, body) => put(`/opportunities/${id}/items/${itemId}`, body),
        removeItem: (id, itemId) => del(`/opportunities/${id}/items/${itemId}`),
        moveTo: (id, stage) => post(`/opportunities/${id}/stage`, { stage }),
        probability: (id, probability) => post(`/opportunities/${id}/probability`, { probability }),
        win: (id) => post(`/opportunities/${id}/win`),
        lose: (id, reason) => post(`/opportunities/${id}/lose`, { reason }),
        reopen: (id) => post(`/opportunities/${id}/reopen`),
        decideDiscount: (id, body) => post(`/opportunities/${id}/discount-decision`, body),
        archive: (id) => post(`/opportunities/${id}/archive`),
        revisions: (id) => get(`/opportunities/${id}/revisions`),
    },
    activities: {
        list: (params) => get("/activities", params),
        summary: (ownerId) => get("/activities/summary", { ownerId }),
        create: (body) => post("/activities", body),
        complete: (id, body) => post(`/activities/${id}/complete`, body),
        cancel: (id, reason) => post(`/activities/${id}/cancel`, { reason }),
        reopen: (id) => post(`/activities/${id}/reopen`),
    },
    notifications: {
        list: (params) => get("/notifications", params),
        unreadCount: (recipientId) => get("/notifications/unread-count", { recipientId }),
        read: (id, recipientId) => post(`/notifications/${id}/read${query({ recipientId })}`),
        readAll: (recipientId) => post(`/notifications/read-all${query({ recipientId })}`),
        stats: () => get("/notifications/stats"),
        preferences: (salesRepId) => get(`/notification-preferences/${salesRepId}`),
        updatePreferences: (salesRepId, emailEnabled) => put(`/notification-preferences/${salesRepId}`, { emailEnabled }),
    },
    platform: {
        health: () => get("/platform/health"),
        messagingStatus: (service) => get(`/platform/${service}/messaging/status`),
        outbox: (service) => get(`/platform/${service}/messaging/outbox`),
        replay: (service, queue) => post(`/platform/${service}/messaging/dead-letters/${queue}/replay`),
    },
};
