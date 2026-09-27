"use client";

import { History } from "lucide-react";
import { useApi } from "@/lib/useApi";
import { dateTime } from "@/lib/format";
import { EmptyState, ErrorBanner, Spinner } from "@/components/ui/Feedback";

const TYPES = { INSERT: "Criação", UPDATE: "Alteração", DELETE: "Exclusão" };

export default function HistoryTimeline({ loader, describe }) {
    const { data, loading, error, reload } = useApi(loader, []);

    if (loading) return <Spinner />;
    if (error) return <ErrorBanner error={error} onRetry={reload} />;
    if (!data?.length) return <EmptyState icon={History} title="Sem histórico" />;

    return (
        <ol className="relative space-y-5 border-l border-surface-700 pl-6">
            {[...data].reverse().map((revision) => (
                <li key={revision.revision} className="relative">
                    <span className="absolute -left-[31px] top-1 h-3 w-3 rounded-full border-2 border-surface-900 bg-brand-500" />
                    <p className="text-sm font-medium text-white">
                        {TYPES[revision.type] ?? revision.type} · revisão #{revision.revision}
                    </p>
                    <p className="text-xs text-slate-400">
                        {dateTime(revision.timestamp)} por <span className="text-slate-300">{revision.actor}</span>
                    </p>
                    {describe && <p className="mt-1 text-sm text-slate-300">{describe(revision.data)}</p>}
                </li>
            ))}
        </ol>
    );
}
