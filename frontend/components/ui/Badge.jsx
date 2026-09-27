const COLORS = {
    sky: "bg-sky-500/15 text-sky-300 ring-sky-500/30",
    cyan: "bg-cyan-500/15 text-cyan-300 ring-cyan-500/30",
    violet: "bg-violet-500/15 text-violet-300 ring-violet-500/30",
    amber: "bg-amber-500/15 text-amber-300 ring-amber-500/30",
    emerald: "bg-emerald-500/15 text-emerald-300 ring-emerald-500/30",
    rose: "bg-rose-500/15 text-rose-300 ring-rose-500/30",
    slate: "bg-slate-500/15 text-slate-300 ring-slate-500/30",
    brand: "bg-brand-500/15 text-brand-300 ring-brand-500/30",
};

export default function Badge({ color = "slate", children, className = "" }) {
    return (
        <span className={`inline-flex items-center gap-1 whitespace-nowrap rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset ${COLORS[color] ?? COLORS.slate} ${className}`}>
            {children}
        </span>
    );
}

export function LabeledBadge({ list, value }) {
    const item = list.find((entry) => entry.value === value);
    if (!value) return null;
    return <Badge color={item?.color ?? "slate"}>{item?.label ?? value}</Badge>;
}
