"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { Bell, ChevronDown } from "lucide-react";
import { api } from "@/lib/api";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import { Avatar } from "@/components/ui/Layout";
import { SECTIONS } from "@/components/layout/Sidebar";

const TEAM_LABELS = {
    INSIDE_SALES: "Inside Sales",
    FIELD_SALES: "Field Sales",
    PRE_SALES: "Pré-vendas",
    ACCOUNT_MANAGEMENT: "Gestão de contas",
};

function NotificationBell({ userId }) {
    const [unread, setUnread] = useState(0);

    useEffect(() => {
        if (!userId) return undefined;
        let active = true;
        const load = () =>
            api.notifications
                .unreadCount(userId)
                .then((result) => active && setUnread(result.unread))
                .catch(() => active && setUnread(0));
        load();
        const timer = setInterval(load, 10000);
        return () => {
            active = false;
            clearInterval(timer);
        };
    }, [userId]);

    return (
        <Link href="/notifications" className="relative rounded-lg p-2 text-slate-300 hover:bg-surface-800 hover:text-white" aria-label="Notificações">
            <Bell className="h-5 w-5" />
            {unread > 0 && (
                <span className="absolute -right-0.5 -top-0.5 flex h-5 min-w-5 items-center justify-center rounded-full bg-rose-500 px-1 text-[10px] font-bold text-white">
                    {unread > 99 ? "99+" : unread}
                </span>
            )}
        </Link>
    );
}

function UserSwitcher() {
    const { user, activeSalesReps, selectUser, error } = useCurrentUser();

    if (error) {
        return <span className="text-xs text-rose-300">Equipe indisponível</span>;
    }

    return (
        <label className="relative flex items-center gap-3 rounded-lg px-2 py-1.5 hover:bg-surface-800">
            <Avatar name={user?.name ?? "?"} size="sm" />
            <span className="hidden whitespace-nowrap text-left sm:block">
                <span className="block text-xs text-slate-500">Entrar como</span>
                <span className="block text-sm font-medium text-white">
                    {user?.name ?? "Selecione"}
                    {user && (
                        <span className="ml-2 text-xs font-normal text-slate-400">
                            {user.role === "MANAGER" ? "Gestor" : "Vendedor"} · {TEAM_LABELS[user.team] ?? user.team}
                        </span>
                    )}
                </span>
            </span>
            <ChevronDown className="h-4 w-4 text-slate-400" />
            <select
                className="absolute inset-0 cursor-pointer opacity-0"
                value={user?.id ?? ""}
                onChange={(event) => selectUser(event.target.value)}
                aria-label="Selecionar usuário"
            >
                <option value="" disabled>
                    Selecione o vendedor
                </option>
                {activeSalesReps.map((rep) => (
                    <option key={rep.id} value={rep.id}>
                        {rep.name} ({rep.role === "MANAGER" ? "gestor" : "vendedor"})
                    </option>
                ))}
            </select>
        </label>
    );
}

export default function Topbar() {
    const pathname = usePathname();
    const { user } = useCurrentUser();
    const links = SECTIONS.flatMap((section) => section.links);

    return (
        <header className="sticky top-0 z-20 border-b border-surface-800 bg-surface-950/80 backdrop-blur">
            <div className="flex h-16 items-center justify-between gap-4 px-4 sm:px-8">
                <nav className="flex gap-1 overflow-x-auto lg:hidden">
                    {links.map((link) => (
                        <Link
                            key={link.href}
                            href={link.href}
                            className={`rounded-md px-2 py-1 text-xs ${pathname === link.href ? "bg-brand-500/20 text-white" : "text-slate-400"}`}
                        >
                            {link.label}
                        </Link>
                    ))}
                </nav>
                <div className="hidden text-sm text-slate-400 lg:block">
                    {new Intl.DateTimeFormat("pt-BR", { weekday: "long", day: "2-digit", month: "long" }).format(new Date())}
                </div>
                <div className="flex items-center gap-2">
                    <NotificationBell userId={user?.id} />
                    <UserSwitcher />
                </div>
            </div>
        </header>
    );
}
