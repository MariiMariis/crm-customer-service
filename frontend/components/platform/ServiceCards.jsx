import { Database, Rabbit, Server, Settings2 } from "lucide-react";
import { number } from "@/lib/format";
import { exchangeColor } from "@/components/platform/palette";

const STATUS = {
    UP: { label: "Operacional", dot: "bg-emerald-400", ring: "border-surface-700/70" },
    DOWN: { label: "Fora do ar", dot: "bg-rose-500", ring: "border-rose-500/50" },
};

function Component({ icon: Icon, label, status }) {
    const up = status === "UP";
    return (
        <span className={`inline-flex items-center gap-1 rounded-md px-2 py-0.5 text-xs ring-1 ring-inset ${up ? "bg-emerald-500/10 text-emerald-300 ring-emerald-500/30" : "bg-rose-500/10 text-rose-300 ring-rose-500/30"}`}>
            <Icon className="h-3 w-3" /> {label}
        </span>
    );
}

function Metric({ label, value, tone = "text-white" }) {
    return (
        <div>
            <p className="whitespace-nowrap text-[11px] uppercase tracking-wide text-slate-500">{label}</p>
            <p className={`text-lg font-semibold ${tone}`}>{value}</p>
        </div>
    );
}

export default function ServiceCards({ services }) {
    return (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {services.map((service) => {
                const status = STATUS[service.status] ?? { label: service.status, dot: "bg-amber-400", ring: "border-amber-500/50" };
                const messaging = service.messaging;
                const Icon = service.name === "config-server" ? Settings2 : Server;
                return (
                    <div key={service.name} className={`card border p-5 ${status.ring}`}>
                        <div className="flex items-start justify-between gap-3">
                            <div className="flex min-w-0 items-center gap-3">
                                <div className="rounded-lg bg-surface-800 p-2 text-slate-300">
                                    <Icon className="h-4 w-4" />
                                </div>
                                <div className="min-w-0">
                                    <p className="truncate font-medium text-white" title={service.name}>{service.name}</p>
                                    <p className="text-xs text-slate-500">{service.url.replace("http://", "")}</p>
                                </div>
                            </div>
                            <span className="inline-flex shrink-0 items-center gap-2 whitespace-nowrap text-xs text-slate-300">
                                <span className={`h-2 w-2 rounded-full ${status.dot} ${service.status === "UP" ? "animate-pulse" : ""}`} />
                                {status.label}
                            </span>
                        </div>
                        <div className="mt-3 flex flex-wrap items-center gap-1.5">
                            {service.components?.db && <Component icon={Database} label="PostgreSQL" status={service.components.db} />}
                            {service.components?.rabbit && <Component icon={Rabbit} label="RabbitMQ" status={service.components.rabbit} />}
                            <span className="ml-auto text-xs text-slate-500">{service.latencyMs} ms</span>
                        </div>
                        {service.status !== "UP" ? (
                            <p className="mt-4 text-sm text-rose-300/90">Sem resposta. O gateway abre o circuit breaker e devolve fallback para as rotas deste serviço.</p>
                        ) : messaging ? (
                            <>
                                <div className="mt-4 grid grid-cols-3 gap-3 border-t border-surface-700/60 pt-4">
                                    <Metric label="No outbox" value={number(messaging.outboxPending)} tone={messaging.outboxPending > 0 ? "text-amber-300" : "text-white"} />
                                    <Metric label="Publicados" value={number(messaging.published)} />
                                    <Metric label="Na DLQ" value={number(messaging.deadLetters)} tone={messaging.deadLetters > 0 ? "text-rose-300" : "text-white"} />
                                </div>
                                {messaging.exchange && (
                                    <div className="mt-3 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-slate-400">
                                        <span className="inline-flex items-center gap-1.5 whitespace-nowrap">
                                            <span className="h-2 w-2 rounded-full" style={{ backgroundColor: exchangeColor(messaging.exchange) }} />
                                            publica em <code className="text-slate-200">{messaging.exchange}</code>
                                        </span>
                                        <span className="whitespace-nowrap">consome {messaging.queues.length} fila(s)</span>
                                    </div>
                                )}
                            </>
                        ) : (
                            <p className="mt-4 text-sm text-slate-400">Configuração centralizada dos microsserviços (Spring Cloud Config).</p>
                        )}
                    </div>
                );
            })}
        </div>
    );
}
