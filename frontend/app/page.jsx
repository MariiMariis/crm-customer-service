"use client";

import Link from "next/link";
import { Briefcase, Building2, Columns3, Target } from "lucide-react";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import { PageHeader } from "@/components/ui/Layout";
import { ErrorBanner } from "@/components/ui/Feedback";

const SHORTCUTS = [
    { href: "/pipeline", label: "Pipeline", description: "Funil de oportunidades em Kanban", icon: Columns3 },
    { href: "/leads", label: "Leads", description: "Qualifique e converta prospects", icon: Target },
    { href: "/opportunities", label: "Oportunidades", description: "Propostas, itens e descontos", icon: Briefcase },
    { href: "/companies", label: "Empresas", description: "Carteira de contas e contatos", icon: Building2 },
];

export default function HomePage() {
    const { user, error } = useCurrentUser();

    return (
        <>
            <PageHeader
                title={user ? `Olá, ${user.name.split(" ")[0]}` : "Bem-vindo ao Nexo"}
                subtitle="Acompanhe leads, oportunidades e contas da equipe comercial."
            />
            <ErrorBanner error={error} />
            <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
                {SHORTCUTS.map((shortcut) => (
                    <Link key={shortcut.href} href={shortcut.href} className="card group p-5 transition hover:border-brand-500/50">
                        <shortcut.icon className="h-6 w-6 text-brand-400" />
                        <p className="mt-4 font-semibold text-white group-hover:text-brand-300">{shortcut.label}</p>
                        <p className="mt-1 text-sm text-slate-400">{shortcut.description}</p>
                    </Link>
                ))}
            </div>
        </>
    );
}
