const currency = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
const compactCurrency = new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
    notation: "compact",
    maximumFractionDigits: 1,
});
const integer = new Intl.NumberFormat("pt-BR");
const dateFormat = new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "2-digit", year: "numeric" });
const dateTimeFormat = new Intl.DateTimeFormat("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
});
const relative = new Intl.RelativeTimeFormat("pt-BR", { numeric: "auto" });

export const money = (value) => currency.format(Number(value ?? 0));

export const moneyCompact = (value) => compactCurrency.format(Number(value ?? 0));

export const number = (value) => integer.format(Number(value ?? 0));

export const percent = (value) => `${Number(value ?? 0).toLocaleString("pt-BR", { maximumFractionDigits: 2 })}%`;

export const date = (value) => (value ? dateFormat.format(new Date(value.length === 10 ? `${value}T12:00:00` : value)) : "-");

export const dateTime = (value) => (value ? dateTimeFormat.format(new Date(value)) : "-");

export function timeAgo(value) {
    if (!value) return "-";
    const seconds = Math.round((new Date(value).getTime() - Date.now()) / 1000);
    const units = [
        ["year", 31536000],
        ["month", 2592000],
        ["day", 86400],
        ["hour", 3600],
        ["minute", 60],
    ];
    for (const [unit, size] of units) {
        if (Math.abs(seconds) >= size) {
            return relative.format(Math.round(seconds / size), unit);
        }
    }
    return relative.format(seconds, "second");
}

export const initials = (name) =>
    (name ?? "?")
        .split(" ")
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part[0].toUpperCase())
        .join("");

export const toDateInput = (date) => new Date(date).toISOString().slice(0, 10);

export const addDays = (days) => {
    const result = new Date();
    result.setDate(result.getDate() + days);
    return toDateInput(result);
};
