export const OPPORTUNITY_STAGES = [
    { value: "PROSPECTING", label: "Prospecção", color: "sky" },
    { value: "QUALIFICATION", label: "Qualificação", color: "cyan" },
    { value: "PROPOSAL", label: "Proposta", color: "violet" },
    { value: "NEGOTIATION", label: "Negociação", color: "amber" },
    { value: "WON", label: "Ganha", color: "emerald" },
    { value: "LOST", label: "Perdida", color: "rose" },
];

export const OPEN_STAGES = OPPORTUNITY_STAGES.filter((stage) => !["WON", "LOST"].includes(stage.value));

export const LEAD_STATUSES = [
    { value: "NEW", label: "Novo", color: "sky" },
    { value: "CONTACTED", label: "Contatado", color: "cyan" },
    { value: "QUALIFIED", label: "Qualificado", color: "violet" },
    { value: "CONVERTING", label: "Convertendo", color: "amber" },
    { value: "CONVERTED", label: "Convertido", color: "emerald" },
    { value: "UNQUALIFIED", label: "Desqualificado", color: "rose" },
];

export const LEAD_SOURCES = [
    { value: "WEBSITE", label: "Site" },
    { value: "REFERRAL", label: "Indicação" },
    { value: "EVENT", label: "Evento" },
    { value: "COLD_CALL", label: "Prospecção ativa" },
    { value: "LINKEDIN", label: "LinkedIn" },
    { value: "PARTNER", label: "Parceiro" },
    { value: "CAMPAIGN", label: "Campanha" },
];

export const INDUSTRIES = [
    { value: "TECHNOLOGY", label: "Tecnologia" },
    { value: "FINANCIAL_SERVICES", label: "Serviços financeiros" },
    { value: "HEALTHCARE", label: "Saúde" },
    { value: "RETAIL", label: "Varejo" },
    { value: "MANUFACTURING", label: "Indústria" },
    { value: "EDUCATION", label: "Educação" },
    { value: "GOVERNMENT", label: "Governo" },
    { value: "LOGISTICS", label: "Logística" },
    { value: "AGRIBUSINESS", label: "Agronegócio" },
    { value: "ENERGY", label: "Energia" },
    { value: "TELECOM", label: "Telecom" },
    { value: "PROFESSIONAL_SERVICES", label: "Serviços profissionais" },
    { value: "OTHER", label: "Outros" },
];

export const COMPANY_SIZES = [
    { value: "MICRO", label: "Micro" },
    { value: "SMALL", label: "Pequena" },
    { value: "MEDIUM", label: "Média" },
    { value: "LARGE", label: "Grande" },
    { value: "ENTERPRISE", label: "Corporativa" },
];

export const COMPANY_TYPES = [
    { value: "PROSPECT", label: "Prospect", color: "sky" },
    { value: "CUSTOMER", label: "Cliente", color: "emerald" },
    { value: "PARTNER", label: "Parceiro", color: "violet" },
    { value: "FORMER_CUSTOMER", label: "Ex-cliente", color: "slate" },
];

export const DECISION_ROLES = [
    { value: "DECISION_MAKER", label: "Decisor", color: "amber" },
    { value: "CHAMPION", label: "Champion", color: "emerald" },
    { value: "INFLUENCER", label: "Influenciador", color: "violet" },
    { value: "TECHNICAL_EVALUATOR", label: "Avaliador técnico", color: "cyan" },
    { value: "USER", label: "Usuário", color: "slate" },
];

export const STATES = ["AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"]
    .map((uf) => ({ value: uf, label: uf }));

export const ACTIVITY_TYPES = [
    { value: "TASK", label: "Tarefa" },
    { value: "CALL", label: "Ligação" },
    { value: "MEETING", label: "Reunião" },
    { value: "EMAIL", label: "E-mail" },
];

export const ACTIVITY_STATUSES = [
    { value: "PLANNED", label: "Planejada", color: "sky" },
    { value: "DONE", label: "Concluída", color: "emerald" },
    { value: "CANCELED", label: "Cancelada", color: "slate" },
];

export const DISCOUNT_APPROVAL = [
    { value: "NOT_REQUIRED", label: "Dentro do limite", color: "slate" },
    { value: "PENDING", label: "Aguardando gestor", color: "amber" },
    { value: "APPROVED", label: "Aprovado", color: "emerald" },
    { value: "REJECTED", label: "Rejeitado", color: "rose" },
];

export const BILLING = [
    { value: "ONE_TIME", label: "Único" },
    { value: "MONTHLY", label: "Mensal" },
    { value: "ANNUAL", label: "Anual" },
];

export const PRODUCT_CATEGORIES = [
    { value: "SOFTWARE", label: "Software", color: "violet" },
    { value: "HARDWARE", label: "Hardware", color: "cyan" },
    { value: "SERVICE", label: "Serviço", color: "amber" },
];

export function labelOf(list, value) {
    return list.find((item) => item.value === value)?.label ?? value ?? "-";
}

export function colorOf(list, value) {
    return list.find((item) => item.value === value)?.color ?? "slate";
}
