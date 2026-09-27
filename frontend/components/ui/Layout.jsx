"use client";

import { ChevronLeft, ChevronRight } from "lucide-react";
import { initials } from "@/lib/format";

export function PageHeader({ title, subtitle, actions, children }) {
    return (
        <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
            <div>
                <h1 className="text-2xl font-semibold tracking-tight text-white">{title}</h1>
                {subtitle && <p className="mt-1 text-sm text-slate-400">{subtitle}</p>}
                {children}
            </div>
            {actions && <div className="flex flex-wrap items-center gap-2">{actions}</div>}
        </div>
    );
}

export function Section({ title, actions, children, className = "", bodyClassName = "p-5" }) {
    return (
        <section className={`card ${className}`}>
            {(title || actions) && (
                <div className="flex items-center justify-between gap-3 border-b border-surface-700/70 px-5 py-3.5">
                    <h2 className="text-sm font-semibold text-white">{title}</h2>
                    {actions && <div className="flex items-center gap-2">{actions}</div>}
                </div>
            )}
            <div className={bodyClassName}>{children}</div>
        </section>
    );
}

export function KpiCard({ label, value, hint, icon: Icon, tone = "brand" }) {
    const tones = {
        brand: "text-brand-400 bg-brand-500/10",
        emerald: "text-emerald-400 bg-emerald-500/10",
        amber: "text-amber-400 bg-amber-500/10",
        rose: "text-rose-400 bg-rose-500/10",
        violet: "text-violet-400 bg-violet-500/10",
    };
    return (
        <div className="card p-5">
            <div className="flex items-start justify-between gap-3">
                <div>
                    <p className="text-xs font-medium uppercase tracking-wide text-slate-400">{label}</p>
                    <p className="mt-2 text-2xl font-semibold text-white">{value}</p>
                    {hint && <p className="mt-1 text-xs text-slate-400">{hint}</p>}
                </div>
                {Icon && (
                    <div className={`rounded-lg p-2.5 ${tones[tone]}`}>
                        <Icon className="h-5 w-5" />
                    </div>
                )}
            </div>
        </div>
    );
}

export function Tabs({ tabs, active, onChange }) {
    return (
        <div className="mb-5 flex gap-1 overflow-x-auto border-b border-surface-700">
            {tabs.map((tab) => (
                <button
                    key={tab.value}
                    onClick={() => onChange(tab.value)}
                    className={`-mb-px whitespace-nowrap border-b-2 px-4 py-2.5 text-sm font-medium transition ${
                        active === tab.value
                            ? "border-brand-500 text-white"
                            : "border-transparent text-slate-400 hover:text-slate-200"
                    }`}
                >
                    {tab.label}
                    {tab.count !== undefined && (
                        <span className="ml-2 rounded-full bg-surface-800 px-2 py-0.5 text-xs text-slate-300">{tab.count}</span>
                    )}
                </button>
            ))}
        </div>
    );
}

export function Pagination({ page, onChange }) {
    if (!page || page.totalPages <= 1) return null;
    return (
        <div className="flex items-center justify-between border-t border-surface-700/70 px-5 py-3 text-sm text-slate-400">
            <span>
                {page.totalElements} registro(s) · página {page.page + 1} de {page.totalPages}
            </span>
            <div className="flex gap-1">
                <button
                    disabled={page.first}
                    onClick={() => onChange(page.page - 1)}
                    className="rounded-md p-1.5 hover:bg-surface-800 disabled:opacity-30"
                    aria-label="Página anterior"
                >
                    <ChevronLeft className="h-4 w-4" />
                </button>
                <button
                    disabled={page.last}
                    onClick={() => onChange(page.page + 1)}
                    className="rounded-md p-1.5 hover:bg-surface-800 disabled:opacity-30"
                    aria-label="Próxima página"
                >
                    <ChevronRight className="h-4 w-4" />
                </button>
            </div>
        </div>
    );
}

export function Avatar({ name, size = "md" }) {
    const sizes = { sm: "h-7 w-7 text-[11px]", md: "h-9 w-9 text-xs", lg: "h-12 w-12 text-sm" };
    return (
        <span className={`inline-flex shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-brand-500 to-violet-600 font-semibold text-white ${sizes[size]}`}>
            {initials(name)}
        </span>
    );
}

export function DetailList({ items }) {
    return (
        <dl className="grid grid-cols-1 gap-x-6 gap-y-4 sm:grid-cols-2">
            {items.filter(Boolean).map((item) => (
                <div key={item.label}>
                    <dt className="text-xs uppercase tracking-wide text-slate-500">{item.label}</dt>
                    <dd className="mt-1 text-sm text-slate-200">{item.value ?? "-"}</dd>
                </div>
            ))}
        </dl>
    );
}
