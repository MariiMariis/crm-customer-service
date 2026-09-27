"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { BadgeDollarSign, Bell, Check, ThumbsDown, Trophy, UserPlus } from "lucide-react";
import { api } from "@/lib/api";
import { useApi } from "@/lib/useApi";
import { NOTIFICATION_TYPES } from "@/lib/labels";
import { dateTime, timeAgo } from "@/lib/format";
import { useToast } from "@/components/providers/ToastProvider";
import Button from "@/components/ui/Button";
import { Select } from "@/components/ui/Field";
import { EmptyState, ErrorBanner, Spinner } from "@/components/ui/Feedback";
import { Pagination } from "@/components/ui/Layout";
import { notifyNotificationsChanged } from "@/components/notifications/events";

const TYPE_ICONS = {
    LEAD_ASSIGNED: { icon: UserPlus, tone: "text-sky-300 bg-sky-500/10" },
    DISCOUNT_APPROVAL_REQUESTED: { icon: BadgeDollarSign, tone: "text-amber-300 bg-amber-500/10" },
    OPPORTUNITY_WON: { icon: Trophy, tone: "text-emerald-300 bg-emerald-500/10" },
    OPPORTUNITY_LOST: { icon: ThumbsDown, tone: "text-rose-300 bg-rose-500/10" },
};

export default function InboxTab({ user, onChanged }) {
    const router = useRouter();
    const toast = useToast();
    const [unreadOnly, setUnreadOnly] = useState(false);
    const [type, setType] = useState("");
    const [page, setPage] = useState(0);
    const { data, loading, error, reload } = useApi(
        () => api.notifications.list({ recipientId: user.id, channel: "IN_APP", unreadOnly, type, page, pageSize: 15 }),
        [user.id, unreadOnly, type, page]
    );

    const changed = () => {
        reload();
        onChanged();
        notifyNotificationsChanged();
    };

    const markRead = async (notification) => {
        try {
            await api.notifications.read(notification.id, user.id);
            changed();
        } catch (err) {
            toast.error(err);
        }
    };

    const open = async (notification) => {
        if (notification.unread) {
            await api.notifications.read(notification.id, user.id).catch(() => null);
            notifyNotificationsChanged();
        }
        if (notification.link) router.push(notification.link);
    };

    const markAll = async () => {
        try {
            const result = await api.notifications.readAll(user.id);
            toast.success(result.marked > 0 ? `${result.marked} notificação(ões) marcada(s) como lida(s)` : "Nada para marcar");
            changed();
        } catch (err) {
            toast.error(err);
        }
    };

    return (
        <div className="card">
            <div className="flex flex-col gap-3 border-b border-surface-700/70 p-4 sm:flex-row sm:items-center">
                <div className="flex rounded-lg bg-surface-850 p-1 ring-1 ring-inset ring-surface-700">
                    {[
                        { value: false, label: "Todas" },
                        { value: true, label: "Não lidas" },
                    ].map((option) => (
                        <button
                            key={option.label}
                            onClick={() => {
                                setUnreadOnly(option.value);
                                setPage(0);
                            }}
                            className={`rounded-md px-3 py-1.5 text-sm transition ${unreadOnly === option.value ? "bg-surface-700 text-white" : "text-slate-400 hover:text-slate-200"}`}
                        >
                            {option.label}
                        </button>
                    ))}
                </div>
                <Select
                    className="sm:w-60"
                    options={NOTIFICATION_TYPES}
                    placeholder="Todos os tipos"
                    value={type}
                    onChange={(event) => {
                        setType(event.target.value);
                        setPage(0);
                    }}
                />
                <div className="sm:ml-auto">
                    <Button variant="secondary" size="sm" icon={Check} onClick={markAll}>Marcar todas como lidas</Button>
                </div>
            </div>
            <ErrorBanner error={error} onRetry={reload} />
            {loading && !data ? (
                <Spinner />
            ) : data?.content.length === 0 ? (
                <EmptyState
                    icon={Bell}
                    title={unreadOnly ? "Tudo em dia" : "Nenhuma notificação"}
                    description="Alertas chegam por eventos: lead atribuído, desconto aguardando aprovação e oportunidades ganhas ou perdidas."
                />
            ) : (
                <ul className="divide-y divide-surface-700/60">
                    {data?.content.map((notification) => {
                        const { icon: Icon, tone } = TYPE_ICONS[notification.type] ?? { icon: Bell, tone: "text-slate-300 bg-surface-800" };
                        return (
                            <li key={notification.id} className={`flex gap-4 px-5 py-4 transition hover:bg-surface-850 ${notification.unread ? "" : "opacity-70"}`}>
                                <div className={`mt-0.5 h-fit rounded-lg p-2 ${tone}`}>
                                    <Icon className="h-4 w-4" />
                                </div>
                                <button onClick={() => open(notification)} className="min-w-0 flex-1 text-left">
                                    <div className="flex items-center gap-2">
                                        {notification.unread && <span className="h-2 w-2 shrink-0 rounded-full bg-brand-400" />}
                                        <p className={`truncate text-sm ${notification.unread ? "font-semibold text-white" : "text-slate-200"}`}>{notification.title}</p>
                                    </div>
                                    <p className="mt-0.5 text-sm text-slate-400">{notification.message}</p>
                                    <p className="mt-1 text-xs text-slate-500" title={dateTime(notification.createdAt)}>{timeAgo(notification.createdAt)}</p>
                                </button>
                                {notification.unread && (
                                    <button
                                        onClick={() => markRead(notification)}
                                        className="h-fit rounded-md p-1.5 text-slate-400 hover:bg-surface-800 hover:text-white"
                                        aria-label="Marcar como lida"
                                        title="Marcar como lida"
                                    >
                                        <Check className="h-4 w-4" />
                                    </button>
                                )}
                            </li>
                        );
                    })}
                </ul>
            )}
            <Pagination page={data} onChange={setPage} />
        </div>
    );
}
