"use client";

import { createContext, useCallback, useContext, useMemo, useState } from "react";
import { CheckCircle2, XCircle } from "lucide-react";

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
    const [toasts, setToasts] = useState([]);

    const push = useCallback((kind, message) => {
        const id = crypto.randomUUID();
        setToasts((current) => [...current, { id, kind, message }]);
        setTimeout(() => setToasts((current) => current.filter((toast) => toast.id !== id)), 4500);
    }, []);

    const value = useMemo(
        () => ({
            success: (message) => push("success", message),
            error: (error) => push("error", typeof error === "string" ? error : error?.message ?? "Erro inesperado"),
        }),
        [push]
    );

    return (
        <ToastContext.Provider value={value}>
            {children}
            <div className="pointer-events-none fixed bottom-5 right-5 z-[60] flex w-full max-w-sm flex-col gap-2">
                {toasts.map((toast) => (
                    <div
                        key={toast.id}
                        className={`pointer-events-auto flex animate-fade-in items-start gap-3 rounded-lg border px-4 py-3 text-sm shadow-xl ${
                            toast.kind === "success"
                                ? "border-emerald-500/30 bg-surface-850 text-emerald-100"
                                : "border-rose-500/30 bg-surface-850 text-rose-100"
                        }`}
                    >
                        {toast.kind === "success" ? (
                            <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-emerald-400" />
                        ) : (
                            <XCircle className="mt-0.5 h-4 w-4 shrink-0 text-rose-400" />
                        )}
                        <span>{toast.message}</span>
                    </div>
                ))}
            </div>
        </ToastContext.Provider>
    );
}

export function useToast() {
    return useContext(ToastContext);
}
