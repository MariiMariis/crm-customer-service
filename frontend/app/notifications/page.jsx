"use client";

import { useState } from "react";
import { UserRound } from "lucide-react";
import { api } from "@/lib/api";
import { useApi } from "@/lib/useApi";
import { useCurrentUser } from "@/components/providers/CurrentUserProvider";
import { EmptyState } from "@/components/ui/Feedback";
import { PageHeader, Tabs } from "@/components/ui/Layout";
import InboxTab from "@/components/notifications/InboxTab";
import EmailLogTab from "@/components/notifications/EmailLogTab";
import PreferencesTab from "@/components/notifications/PreferencesTab";

export default function NotificationsPage() {
    const { user } = useCurrentUser();
    const [tab, setTab] = useState("inbox");
    const { data: unread, reload: reloadUnread } = useApi(
        () => (user ? api.notifications.unreadCount(user.id) : Promise.resolve(null)),
        [user?.id]
    );

    return (
        <>
            <PageHeader
                title="Notificações"
                subtitle="Alertas gerados a partir dos eventos de vendas (pub/sub) e entregues por uma fila de trabalho com retry e DLQ."
            />
            {!user ? (
                <div className="card">
                    <EmptyState icon={UserRound} title="Selecione um usuário" description="Use o seletor 'Entrar como' no topo para ver a caixa de entrada de um vendedor." />
                </div>
            ) : (
                <>
                    <Tabs
                        active={tab}
                        onChange={setTab}
                        tabs={[
                            { value: "inbox", label: "Caixa de entrada", count: unread?.unread ?? undefined },
                            { value: "emails", label: "E-mails enviados" },
                            { value: "preferences", label: "Preferências" },
                        ]}
                    />
                    {tab === "inbox" && <InboxTab key={user.id} user={user} onChanged={reloadUnread} />}
                    {tab === "emails" && <EmailLogTab />}
                    {tab === "preferences" && <PreferencesTab key={user.id} user={user} />}
                </>
            )}
        </>
    );
}
