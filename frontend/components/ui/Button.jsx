import Link from "next/link";
import { Loader2 } from "lucide-react";

const VARIANTS = {
    primary: "bg-brand-500 text-white hover:bg-brand-400 shadow-lg shadow-brand-500/20",
    secondary: "bg-surface-800 text-slate-200 hover:bg-surface-700 ring-1 ring-inset ring-surface-600",
    ghost: "text-slate-300 hover:bg-surface-800 hover:text-white",
    danger: "bg-rose-600/90 text-white hover:bg-rose-500",
    success: "bg-emerald-600 text-white hover:bg-emerald-500",
};

const SIZES = {
    sm: "px-2.5 py-1.5 text-xs",
    md: "px-3.5 py-2 text-sm",
};

export default function Button({
    variant = "primary",
    size = "md",
    icon: Icon,
    loading = false,
    href,
    className = "",
    children,
    ...props
}) {
    const classes = `inline-flex items-center justify-center gap-2 rounded-lg font-medium transition disabled:cursor-not-allowed disabled:opacity-50 ${VARIANTS[variant]} ${SIZES[size]} ${className}`;
    const content = (
        <>
            {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : Icon ? <Icon className="h-4 w-4" /> : null}
            {children}
        </>
    );
    if (href) {
        return (
            <Link href={href} className={classes}>
                {content}
            </Link>
        );
    }
    return (
        <button className={classes} disabled={loading || props.disabled} {...props}>
            {content}
        </button>
    );
}
