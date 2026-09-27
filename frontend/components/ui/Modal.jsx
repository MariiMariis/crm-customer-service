"use client";

import { useEffect } from "react";
import { X } from "lucide-react";

export default function Modal({ open, title, description, onClose, children, footer, size = "md" }) {
    useEffect(() => {
        if (!open) return undefined;
        const onKey = (event) => event.key === "Escape" && onClose?.();
        window.addEventListener("keydown", onKey);
        return () => window.removeEventListener("keydown", onKey);
    }, [open, onClose]);

    if (!open) return null;
    const width = size === "lg" ? "max-w-3xl" : size === "sm" ? "max-w-md" : "max-w-xl";

    return (
        <div className="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-black/60 p-4 backdrop-blur-sm sm:items-center">
            <div className={`card w-full ${width} animate-fade-in`} role="dialog" aria-modal="true">
                <div className="flex items-start justify-between gap-4 border-b border-surface-700 px-5 py-4">
                    <div>
                        <h2 className="text-base font-semibold text-white">{title}</h2>
                        {description && <p className="mt-0.5 text-sm text-slate-400">{description}</p>}
                    </div>
                    <button onClick={onClose} className="rounded-md p-1 text-slate-400 hover:bg-surface-800 hover:text-white" aria-label="Fechar">
                        <X className="h-4 w-4" />
                    </button>
                </div>
                <div className="px-5 py-4">{children}</div>
                {footer && <div className="flex justify-end gap-2 border-t border-surface-700 px-5 py-3">{footer}</div>}
            </div>
        </div>
    );
}
