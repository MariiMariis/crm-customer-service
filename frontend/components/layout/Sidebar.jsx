"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
    Activity,
    Bell,
    Briefcase,
    Building2,
    Columns3,
    LayoutDashboard,
    Target,
    Users,
} from "lucide-react";

const SECTIONS = [
    {
        title: "Vendas",
        links: [
            { href: "/", label: "Dashboard", icon: LayoutDashboard },
            { href: "/pipeline", label: "Pipeline", icon: Columns3 },
            { href: "/leads", label: "Leads", icon: Target },
            { href: "/opportunities", label: "Oportunidades", icon: Briefcase },
        ],
    },
    {
        title: "Relacionamento",
        links: [
            { href: "/companies", label: "Empresas", icon: Building2 },
            { href: "/contacts", label: "Contatos", icon: Users },
        ],
    },
    {
        title: "Sistema",
        links: [
            { href: "/notifications", label: "Notificações", icon: Bell },
            { href: "/platform", label: "Plataforma", icon: Activity },
        ],
    },
];

export default function Sidebar() {
    const pathname = usePathname();
    const isActive = (href) => (href === "/" ? pathname === "/" : pathname.startsWith(href));

    return (
        <aside className="fixed inset-y-0 left-0 z-30 hidden w-60 flex-col border-r border-surface-800 bg-surface-900/80 backdrop-blur lg:flex">
            <Link href="/" className="flex items-center gap-3 px-5 py-5">
                <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-gradient-to-br from-brand-400 to-brand-700 text-sm font-bold text-white shadow-lg shadow-brand-500/30">
                    NX
                </span>
                <span>
                    <span className="block text-sm font-semibold tracking-wide text-white">Nexo</span>
                    <span className="block text-xs text-slate-400">Cada negócio, conectado.</span>
                </span>
            </Link>
            <nav className="flex-1 space-y-6 overflow-y-auto px-3 py-2">
                {SECTIONS.map((section) => (
                    <div key={section.title}>
                        <p className="px-3 pb-2 text-[11px] font-semibold uppercase tracking-wider text-slate-500">{section.title}</p>
                        <ul className="space-y-0.5">
                            {section.links.map((link) => (
                                <li key={link.href}>
                                    <Link
                                        href={link.href}
                                        className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition ${
                                            isActive(link.href)
                                                ? "bg-brand-500/15 font-medium text-white ring-1 ring-inset ring-brand-500/30"
                                                : "text-slate-400 hover:bg-surface-800 hover:text-slate-100"
                                        }`}
                                    >
                                        <link.icon className={`h-4 w-4 ${isActive(link.href) ? "text-brand-400" : ""}`} />
                                        {link.label}
                                    </Link>
                                </li>
                            ))}
                        </ul>
                    </div>
                ))}
            </nav>
            <div className="border-t border-surface-800 px-5 py-4 text-xs text-slate-500">
                TP4 · Arquitetura orientada a eventos
            </div>
        </aside>
    );
}

export { SECTIONS };
