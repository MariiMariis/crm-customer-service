import { AlertTriangle, Inbox, Loader2 } from "lucide-react";

export function Spinner({ label = "Carregando..." }) {
    return (
        <div className="flex items-center justify-center gap-2 py-12 text-sm text-slate-400">
            <Loader2 className="h-4 w-4 animate-spin text-brand-400" />
            {label}
        </div>
    );
}

export function ErrorBanner({ error, onRetry }) {
    if (!error) return null;
    return (
        <div className="flex items-start gap-3 rounded-lg border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-200">
            <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-rose-400" />
            <div className="flex-1">
                <p>{error.message}</p>
                {error.details?.length > 0 && (
                    <ul className="mt-1 list-inside list-disc text-rose-300/80">
                        {error.details.map((detail) => (
                            <li key={detail}>{detail}</li>
                        ))}
                    </ul>
                )}
            </div>
            {onRetry && (
                <button onClick={onRetry} className="text-xs font-medium text-rose-200 underline underline-offset-2">
                    Tentar de novo
                </button>
            )}
        </div>
    );
}

export function EmptyState({ icon: Icon = Inbox, title, description, action }) {
    return (
        <div className="flex flex-col items-center justify-center gap-2 px-6 py-14 text-center">
            <div className="rounded-full bg-surface-800 p-3 text-slate-400">
                <Icon className="h-6 w-6" />
            </div>
            <p className="font-medium text-slate-200">{title}</p>
            {description && <p className="max-w-md text-sm text-slate-400">{description}</p>}
            {action && <div className="mt-2">{action}</div>}
        </div>
    );
}
