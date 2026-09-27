const EXCHANGE_COLORS = {
    "team.events": "#38bdf8",
    "accounts.events": "#a78bfa",
    "catalog.events": "#22d3ee",
    "sales.events": "#f59e0b",
    "notification.events": "#34d399",
};

const FALLBACK_COLORS = ["#f472b6", "#fb7185", "#facc15", "#60a5fa"];

export function exchangeColor(exchange) {
    if (EXCHANGE_COLORS[exchange]) return EXCHANGE_COLORS[exchange];
    const hash = [...(exchange ?? "")].reduce((sum, char) => sum + char.charCodeAt(0), 0);
    return FALLBACK_COLORS[hash % FALLBACK_COLORS.length];
}

export const serviceKey = (name) => name.replace(/-service$/, "");
