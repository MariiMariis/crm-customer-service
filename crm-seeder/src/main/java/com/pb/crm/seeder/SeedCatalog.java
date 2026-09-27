package com.pb.crm.seeder;

import java.math.BigDecimal;
import java.util.List;

public final class SeedCatalog {

    private SeedCatalog() {
    }

    public record RepSeed(String key, String name, String email, String phone, String team, String role,
                          Integer quota, String managerKey) {
    }

    public record ProductSeed(String key, String name, String description, String subcategory, String billing,
                              String unit, String price, String cost, String maxDiscount, String manufacturer,
                              Integer warrantyMonths) {
    }

    public record ContactSeed(String firstName, String lastName, String emailUser, String jobTitle, String department,
                              String decisionRole, String phone) {
    }

    public record CompanySeed(String key, String legalName, String tradeName, String industry, String size,
                              Integer employees, String annualRevenue, String website, String phone, String city,
                              String state, String type, String ownerKey, List<ContactSeed> contacts) {
    }

    public record ConversionSeed(String industry, String companySize, String city, String state, String opportunityTitle) {
    }

    public record LeadSeed(String firstName, String lastName, String email, String phone, String companyName,
                           String jobTitle, String source, Integer estimatedValue, String ownerKey, String target,
                           String disqualifyReason, ConversionSeed conversion) {
    }

    public record ItemSeed(String productKey, int quantity, String discount) {
    }

    public record OpportunitySeed(String key, String title, String companyKey, int contactIndex, String ownerKey,
                                  int closeInDays, int termMonths, List<ItemSeed> items, String target,
                                  String lossReason, boolean approveDiscount) {
    }

    public record ActivitySeed(String type, String subject, String relatedType, String relatedKey, String ownerKey,
                               int dueInHours, int meetingMinutes, String location, String outcome, Integer callMinutes) {
    }

    public static final List<RepSeed> SALES_REPS = List.of(
            new RepSeed("marina", "Marina Costa", "marina.costa@pbtech.com.br", "11991110001", "FIELD_SALES", "MANAGER", null, null),
            new RepSeed("ricardo", "Ricardo Almeida", "ricardo.almeida@pbtech.com.br", "11991110002", "INSIDE_SALES", "MANAGER", null, null),
            new RepSeed("joao", "Joao Lima", "joao.lima@pbtech.com.br", "11991110003", "FIELD_SALES", "REP", 180000, "marina"),
            new RepSeed("ana", "Ana Ribeiro", "ana.ribeiro@pbtech.com.br", "11991110004", "FIELD_SALES", "REP", 220000, "marina"),
            new RepSeed("felipe", "Felipe Moura", "felipe.moura@pbtech.com.br", "11991110005", "PRE_SALES", "REP", 60000, "marina"),
            new RepSeed("bruno", "Bruno Carvalho", "bruno.carvalho@pbtech.com.br", "11991110006", "INSIDE_SALES", "REP", 90000, "ricardo"),
            new RepSeed("camila", "Camila Duarte", "camila.duarte@pbtech.com.br", "11991110007", "INSIDE_SALES", "REP", 90000, "ricardo"),
            new RepSeed("larissa", "Larissa Nunes", "larissa.nunes@pbtech.com.br", "11991110008", "ACCOUNT_MANAGEMENT", "REP", 150000, "ricardo")
    );

    public static final List<ProductSeed> PRODUCTS = List.of(
            new ProductSeed("m365-bs", "Microsoft 365 Business Standard", "Pacote Office, Teams, Exchange e OneDrive por usuario", "PRODUCTIVITY", "MONTHLY", "USER", "79.90", "62.50", "5", "Microsoft", null),
            new ProductSeed("m365-e3", "Microsoft 365 E3", "Plano corporativo com seguranca e conformidade avancadas", "PRODUCTIVITY", "ANNUAL", "USER", "2340.00", "1890.00", "8", "Microsoft", null),
            new ProductSeed("win11", "Windows 11 Pro (licenca perpetua)", "Licenca OEM/ESD por dispositivo", "INFRASTRUCTURE", "ONE_TIME", "LICENSE", "1450.00", "1120.00", "5", "Microsoft", null),
            new ProductSeed("forticlient", "FortiClient EMS", "Protecao de endpoint e VPN gerenciada", "SECURITY", "ANNUAL", "DEVICE", "210.00", "150.00", "10", "Fortinet", null),
            new ProductSeed("kaspersky", "Kaspersky Endpoint Security Cloud", "Antivirus corporativo em nuvem", "SECURITY", "ANNUAL", "DEVICE", "180.00", "120.00", "12", "Kaspersky", null),
            new ProductSeed("veeam", "Veeam Backup Essentials", "Backup e replicacao de ambientes virtualizados", "INFRASTRUCTURE", "ANNUAL", "LICENSE", "1800.00", "1350.00", "10", "Veeam", null),
            new ProductSeed("vsphere", "VMware vSphere Standard", "Virtualizacao de servidores por processador", "INFRASTRUCTURE", "ANNUAL", "LICENSE", "6500.00", "5200.00", "7", "Broadcom", null),
            new ProductSeed("rhel", "Red Hat Enterprise Linux", "Assinatura de sistema operacional corporativo", "INFRASTRUCTURE", "ANNUAL", "DEVICE", "2800.00", "2250.00", "6", "Red Hat", null),
            new ProductSeed("protheus", "TOTVS Protheus", "ERP completo por usuario nomeado", "ERP", "MONTHLY", "USER", "350.00", "260.00", "10", "TOTVS", null),
            new ProductSeed("github", "GitHub Enterprise", "Repositorios, CI/CD e seguranca de codigo", "DEVELOPMENT", "ANNUAL", "USER", "1260.00", "1010.00", "5", "GitHub", null),
            new ProductSeed("rdstation", "RD Station CRM Pro", "CRM para equipes comerciais", "CRM", "MONTHLY", "USER", "129.00", "95.00", "10", "RD Station", null),
            new ProductSeed("latitude", "Notebook Dell Latitude 5450", "Intel Core Ultra 5, 16 GB, SSD 512 GB", "NOTEBOOK", "ONE_TIME", "UNIT", "8500.00", "6800.00", "8", "Dell", 36),
            new ProductSeed("thinkpad", "Notebook Lenovo ThinkPad T14", "AMD Ryzen 7 PRO, 32 GB, SSD 1 TB", "NOTEBOOK", "ONE_TIME", "UNIT", "9200.00", "7450.00", "8", "Lenovo", 36),
            new ProductSeed("optiplex", "Desktop Dell OptiPlex 7010", "Intel Core i5, 16 GB, SSD 512 GB", "DESKTOP", "ONE_TIME", "UNIT", "5200.00", "4150.00", "8", "Dell", 36),
            new ProductSeed("r760", "Servidor Dell PowerEdge R760", "2x Intel Xeon Silver, 256 GB, 8x SSD 1.92 TB", "SERVER", "ONE_TIME", "UNIT", "65000.00", "52000.00", "6", "Dell", 36),
            new ProductSeed("dl380", "Servidor HPE ProLiant DL380 Gen11", "2x Intel Xeon Gold, 512 GB", "SERVER", "ONE_TIME", "UNIT", "72000.00", "58500.00", "6", "HPE", 36),
            new ProductSeed("me5", "Storage Dell PowerVault ME5024", "Storage SAN com 24 baias", "STORAGE", "ONE_TIME", "UNIT", "95000.00", "78000.00", "5", "Dell", 36),
            new ProductSeed("catalyst", "Switch Cisco Catalyst 9200 48p", "Switch gerenciavel PoE+ 48 portas", "NETWORK", "ONE_TIME", "UNIT", "18500.00", "14200.00", "8", "Cisco", 12),
            new ProductSeed("fortigate", "Firewall Fortinet FortiGate 100F", "NGFW com SD-WAN integrado", "NETWORK", "ONE_TIME", "UNIT", "32000.00", "24500.00", "8", "Fortinet", 12),
            new ProductSeed("u6pro", "Access Point Ubiquiti U6 Pro", "Wi-Fi 6 corporativo", "NETWORK", "ONE_TIME", "UNIT", "1450.00", "1050.00", "10", "Ubiquiti", 12),
            new ProductSeed("monitor", "Monitor Dell 27 P2723D", "QHD 27 polegadas com ajuste de altura", "PERIPHERAL", "ONE_TIME", "UNIT", "1900.00", "1480.00", "10", "Dell", 36),
            new ProductSeed("cloud-consulting", "Consultoria em arquitetura cloud", "Horas de arquiteto certificado Azure/AWS", "CONSULTING", "ONE_TIME", "HOUR", "280.00", "160.00", "10", "PB Tech", null),
            new ProductSeed("erp-rollout", "Implantacao de ERP", "Projeto de implantacao com gestao de mudanca", "IMPLEMENTATION", "ONE_TIME", "PROJECT", "45000.00", "28000.00", "8", "PB Tech", null),
            new ProductSeed("support-247", "Suporte 24x7", "Service desk com SLA de 4 horas", "SUPPORT", "MONTHLY", "UNIT", "4500.00", "2900.00", "10", "PB Tech", null),
            new ProductSeed("soc", "SOC/NOC gerenciado", "Monitoramento de seguranca e rede 24x7", "MANAGED_SERVICES", "MONTHLY", "UNIT", "12000.00", "7800.00", "8", "PB Tech", null),
            new ProductSeed("backup-managed", "Backup gerenciado em nuvem", "Backup diario com retencao de 90 dias", "MANAGED_SERVICES", "MONTHLY", "UNIT", "2500.00", "1500.00", "10", "PB Tech", null),
            new ProductSeed("training-m365", "Treinamento Microsoft 365", "Pacote de 16 horas para usuarios finais", "TRAINING", "ONE_TIME", "HOUR_PACKAGE", "3200.00", "1800.00", "15", "PB Tech", null)
    );

    private static List<ContactSeed> contacts(ContactSeed... contacts) {
        return List.of(contacts);
    }

    public static final List<CompanySeed> COMPANIES = List.of(
            new CompanySeed("santa-clara", "Hospital Santa Clara Ltda", "Hospital Santa Clara", "HEALTHCARE", "LARGE", 1200, "350000000", "santaclara.org.br", "3133330001", "Belo Horizonte", "MG", "PROSPECT", "ana",
                    contacts(new ContactSeed("Paula", "Andrade", "paula.andrade", "Diretora de TI", "Tecnologia", "DECISION_MAKER", "31988880001"),
                            new ContactSeed("Marcos", "Vieira", "marcos.vieira", "Coordenador de Infraestrutura", "Tecnologia", "TECHNICAL_EVALUATOR", "31988880002"),
                            new ContactSeed("Helena", "Souza", "helena.souza", "Gerente de Compras", "Suprimentos", "INFLUENCER", "31988880003"))),
            new CompanySeed("farma-bem", "Rede Farma Bem SA", "Farma Bem", "RETAIL", "LARGE", 2400, "780000000", "farmabem.com.br", "1133330002", "Sao Paulo", "SP", "PROSPECT", "joao",
                    contacts(new ContactSeed("Camila", "Rocha", "camila.rocha", "CIO", "Tecnologia", "DECISION_MAKER", "11988880004"),
                            new ContactSeed("Diego", "Martins", "diego.martins", "Analista de Redes", "Tecnologia", "USER", "11988880005"))),
            new CompanySeed("banco-sul", "Banco Regional Sul SA", "Banco Sul", "FINANCIAL_SERVICES", "ENTERPRISE", 5200, "2100000000", "bancosul.com.br", "5133330003", "Porto Alegre", "RS", "PROSPECT", "ana",
                    contacts(new ContactSeed("Renato", "Farias", "renato.farias", "Superintendente de Tecnologia", "TI", "DECISION_MAKER", "51988880006"),
                            new ContactSeed("Juliana", "Prates", "juliana.prates", "Gerente de Seguranca da Informacao", "Seguranca", "CHAMPION", "51988880007"),
                            new ContactSeed("Otavio", "Reis", "otavio.reis", "Especialista em Data Center", "Infraestrutura", "TECHNICAL_EVALUATOR", "51988880008"))),
            new CompanySeed("horizonte", "Metalurgica Horizonte SA", "Horizonte", "MANUFACTURING", "LARGE", 850, "120000000", "horizonte.ind.br", "4733330004", "Joinville", "SC", "PROSPECT", "joao",
                    contacts(new ContactSeed("Carla", "Mendes", "carla.mendes", "Diretora Administrativa", "Administrativo", "DECISION_MAKER", "47988880009"),
                            new ContactSeed("Tiago", "Albuquerque", "tiago.albuquerque", "Supervisor de TI", "TI", "CHAMPION", "47988880010"))),
            new CompanySeed("veloz", "Transportadora Veloz Ltda", "Veloz Transportes", "LOGISTICS", "MEDIUM", 320, "65000000", "veloztransportes.com.br", "4133330005", "Curitiba", "PR", "PROSPECT", "bruno",
                    contacts(new ContactSeed("Rodrigo", "Nunes", "rodrigo.nunes", "Gerente de Operacoes", "Operacoes", "DECISION_MAKER", "41988880011"),
                            new ContactSeed("Lia", "Campos", "lia.campos", "Analista de Sistemas", "TI", "USER", "41988880012"))),
            new CompanySeed("novo-saber", "Colegio Novo Saber Ltda", "Colegio Novo Saber", "EDUCATION", "MEDIUM", 180, "28000000", "novosaber.edu.br", "2133330006", "Rio de Janeiro", "RJ", "PROSPECT", "camila",
                    contacts(new ContactSeed("Beatriz", "Lopes", "beatriz.lopes", "Diretora Pedagogica", "Direcao", "DECISION_MAKER", "21988880013"),
                            new ContactSeed("Andre", "Pacheco", "andre.pacheco", "Coordenador de TI", "TI", "TECHNICAL_EVALUATOR", "21988880014"))),
            new CompanySeed("campo-verde", "Prefeitura Municipal de Campo Verde", "Prefeitura de Campo Verde", "GOVERNMENT", "LARGE", 1500, null, "campoverde.go.gov.br", "6233330007", "Goiania", "GO", "PROSPECT", "larissa",
                    contacts(new ContactSeed("Sergio", "Batista", "sergio.batista", "Secretario de Tecnologia", "Secretaria de TI", "DECISION_MAKER", "62988880015"),
                            new ContactSeed("Fernanda", "Gomes", "fernanda.gomes", "Pregoeira", "Licitacoes", "INFLUENCER", "62988880016"))),
            new CompanySeed("agrovale", "Cooperativa AgroVale", "AgroVale", "AGRIBUSINESS", "LARGE", 950, "430000000", "agrovale.coop.br", "6533330008", "Cuiaba", "MT", "PROSPECT", "joao",
                    contacts(new ContactSeed("Gustavo", "Teixeira", "gustavo.teixeira", "Gerente de TI", "TI", "DECISION_MAKER", "65988880017"),
                            new ContactSeed("Patricia", "Moraes", "patricia.moraes", "Controller", "Financeiro", "INFLUENCER", "65988880018"))),
            new CompanySeed("solar", "Energia Solar Brasil SA", "Solar Brasil", "ENERGY", "MEDIUM", 400, "210000000", "solarbrasil.com.br", "8533330009", "Fortaleza", "CE", "PROSPECT", "camila",
                    contacts(new ContactSeed("Eduardo", "Pinheiro", "eduardo.pinheiro", "CTO", "Tecnologia", "DECISION_MAKER", "85988880019"),
                            new ContactSeed("Livia", "Barros", "livia.barros", "Gerente de Projetos", "PMO", "CHAMPION", "85988880020"))),
            new CompanySeed("conecta", "Conecta Telecom Ltda", "Conecta", "TELECOM", "MEDIUM", 260, "95000000", "conectatelecom.com.br", "8133330010", "Recife", "PE", "PROSPECT", "bruno",
                    contacts(new ContactSeed("Vinicius", "Araujo", "vinicius.araujo", "Diretor de Operacoes", "Operacoes", "DECISION_MAKER", "81988880021"),
                            new ContactSeed("Natalia", "Freitas", "natalia.freitas", "Engenheira de Redes", "Engenharia", "TECHNICAL_EVALUATOR", "81988880022"))),
            new CompanySeed("moraes-adv", "Moraes e Associados Advocacia", "Moraes Advocacia", "PROFESSIONAL_SERVICES", "SMALL", 45, "12000000", "moraesadv.com.br", "6133330011", "Brasilia", "DF", "PROSPECT", "larissa",
                    contacts(new ContactSeed("Rafael", "Moraes", "rafael.moraes", "Socio Administrador", "Socios", "DECISION_MAKER", "61988880023"))),
            new CompanySeed("estrela", "Lojas Estrela Ltda", "Lojas Estrela", "RETAIL", "MEDIUM", 600, "140000000", "lojasestrela.com.br", "7133330012", "Salvador", "BA", "PROSPECT", "camila",
                    contacts(new ContactSeed("Aline", "Santos", "aline.santos", "Gerente de TI", "TI", "DECISION_MAKER", "71988880024"),
                            new ContactSeed("Caio", "Ferreira", "caio.ferreira", "Comprador", "Compras", "INFLUENCER", "71988880025"))),
            new CompanySeed("vida-plena", "Clinica Vida Plena Ltda", "Clinica Vida Plena", "HEALTHCARE", "SMALL", 70, "18000000", "vidaplena.med.br", "2733330013", "Vitoria", "ES", "PROSPECT", "bruno",
                    contacts(new ContactSeed("Monica", "Alves", "monica.alves", "Administradora", "Administracao", "DECISION_MAKER", "27988880026"))),
            new CompanySeed("pixel", "Pixel Softhouse Ltda", "Pixel", "TECHNOLOGY", "SMALL", 35, "8000000", "pixelsoft.com.br", "1933330014", "Campinas", "SP", "PARTNER", "larissa",
                    contacts(new ContactSeed("Leandro", "Castro", "leandro.castro", "CEO", "Diretoria", "DECISION_MAKER", "19988880027"),
                            new ContactSeed("Bianca", "Ramos", "bianca.ramos", "Gerente de Parcerias", "Comercial", "CHAMPION", "19988880028"))),
            new CompanySeed("alicerce", "Construtora Alicerce Ltda", "Construtora Alicerce", "OTHER", "MEDIUM", 480, "175000000", "alicerce.eng.br", "9233330015", "Manaus", "AM", "FORMER_CUSTOMER", "ana",
                    contacts(new ContactSeed("Hugo", "Siqueira", "hugo.siqueira", "Gerente Administrativo", "Administrativo", "DECISION_MAKER", "92988880029"),
                            new ContactSeed("Tatiane", "Brito", "tatiane.brito", "Analista de TI", "TI", "USER", "92988880030")))
    );

    public static final List<LeadSeed> LEADS = List.of(
            new LeadSeed("Renata", "Lopes", "renata.lopes@lojasalfa.com.br", "11912340001", "Lojas Alfa", "Diretora de TI", "REFERRAL", 250000, "joao", "CONVERTED", null,
                    new ConversionSeed("RETAIL", "LARGE", "Sao Paulo", "SP", "Notebooks e Wi-Fi para 40 lojas")),
            new LeadSeed("Otavio", "Guedes", "otavio.guedes@logmais.com.br", "31912340002", "LogMais Armazens", "Gerente de TI", "EVENT", 120000, "bruno", "CONVERTED", null,
                    new ConversionSeed("LOGISTICS", "MEDIUM", "Contagem", "MG", "Backup gerenciado e firewall")),
            new LeadSeed("Isabela", "Queiroz", "isabela.queiroz@odontosorriso.com.br", "41912340003", "Rede OdontoSorriso", "CEO", "PARTNER", 85000, "camila", "CONVERTED", null,
                    new ConversionSeed("HEALTHCARE", "SMALL", "Londrina", "PR", "Microsoft 365 para 12 clinicas")),
            new LeadSeed("Mauricio", "Sales", "mauricio.sales@grupoatlas.com.br", "11912340004", "Grupo Atlas", "CIO", "LINKEDIN", 600000, "ana", "QUALIFIED", null, null),
            new LeadSeed("Priscila", "Cunha", "priscila.cunha@escolaviva.edu.br", "21912340005", "Escola Viva", "Coordenadora de TI", "WEBSITE", 45000, "camila", "QUALIFIED", null, null),
            new LeadSeed("Fabio", "Dantas", "fabio.dantas@frigorificonorte.com.br", "91912340006", "Frigorifico Norte", "Gerente Industrial", "COLD_CALL", 180000, "joao", "QUALIFIED", null, null),
            new LeadSeed("Luana", "Pires", "luana.pires@startupx.io", "11912340007", "StartupX", "Head de Engenharia", "WEBSITE", 38000, "bruno", "QUALIFIED", null, null),
            new LeadSeed("Roberto", "Menezes", "roberto.menezes@usinapiracicaba.com.br", "19912340008", "Usina Piracicaba", "Diretor Financeiro", "EVENT", 320000, "ana", "CONTACTED", null, null),
            new LeadSeed("Sabrina", "Leal", "sabrina.leal@modaviva.com.br", "47912340009", "Moda Viva Confeccoes", "Gerente Administrativa", "CAMPAIGN", 60000, "camila", "CONTACTED", null, null),
            new LeadSeed("Cesar", "Brandao", "cesar.brandao@hotelmar.com.br", "48912340010", "Hotel Mar Azul", "Gerente Geral", "REFERRAL", 150000, "larissa", "CONTACTED", null, null),
            new LeadSeed("Viviane", "Castro", "viviane.castro@laboratoriovital.com.br", "81912340011", "Laboratorio Vital", "Coordenadora de TI", "LINKEDIN", 95000, "bruno", "CONTACTED", null, null),
            new LeadSeed("Anderson", "Faria", "anderson.faria@autopecasfaria.com.br", "62912340012", "Autopecas Faria", "Socio", "COLD_CALL", 25000, "joao", "NEW", null, null),
            new LeadSeed("Daniela", "Prado", "daniela.prado@contabilprado.com.br", "11912340013", "Prado Contabilidade", "Socia", "WEBSITE", 18000, "camila", "NEW", null, null),
            new LeadSeed("Marcelo", "Tavares", "marcelo.tavares@segurosunion.com.br", "21912340014", "Union Seguros", "Superintendente de TI", "EVENT", 450000, "ana", "NEW", null, null),
            new LeadSeed("Gabriela", "Nogueira", "gabriela.nogueira@petshopamigo.com.br", "31912340015", "PetShop Amigo", "Proprietaria", "CAMPAIGN", 12000, null, "NEW", null, null),
            new LeadSeed("Henrique", "Lacerda", "henrique.lacerda@construtoraprisma.com.br", "85912340016", "Construtora Prisma", "Diretor de Obras", "WEBSITE", 70000, null, "NEW", null, null),
            new LeadSeed("Yasmin", "Correia", "yasmin.correia@universoidiomas.com.br", "71912340017", "Universo Idiomas", "Coordenadora", "LINKEDIN", 22000, null, "NEW", null, null),
            new LeadSeed("Paulo", "Rezende", null, "51912340018", "Rezende Transportes", "Proprietario", "COLD_CALL", 30000, null, "NEW", null, null),
            new LeadSeed("Tereza", "Mota", "tereza.mota@padariaestrela.com.br", "11912340019", "Padaria Estrela", "Proprietaria", "CAMPAIGN", 5000, "camila", "UNQUALIFIED", "empresa muito pequena para o portfolio atual", null),
            new LeadSeed("Igor", "Santana", "igor.santana@gamerhouse.com.br", "11912340020", "GamerHouse", "Comprador", "WEBSITE", 8000, "bruno", "UNQUALIFIED", "busca apenas hardware de varejo, sem projeto corporativo", null)
    );

    public static final List<OpportunitySeed> OPPORTUNITIES = List.of(
            new OpportunitySeed("hsc-infra", "Modernizacao do data center", "santa-clara", 1, "ana", 45, 36,
                    List.of(new ItemSeed("r760", 3, "4"), new ItemSeed("me5", 1, "3"), new ItemSeed("vsphere", 6, "0"), new ItemSeed("veeam", 6, "5")), "NEGOTIATION", null, false),
            new OpportunitySeed("hsc-m365", "Microsoft 365 para o corpo clinico", "santa-clara", 0, "ana", 20, 12,
                    List.of(new ItemSeed("m365-bs", 600, "4"), new ItemSeed("training-m365", 4, "10")), "WON", null, false),
            new OpportunitySeed("farma-wifi", "Wi-Fi gerenciado em 120 lojas", "farma-bem", 0, "joao", 30, 24,
                    List.of(new ItemSeed("u6pro", 240, "15"), new ItemSeed("fortigate", 2, "5"), new ItemSeed("support-247", 1, "0")), "NEGOTIATION", null, false),
            new OpportunitySeed("farma-notebooks", "Renovacao de notebooks da matriz", "farma-bem", 0, "joao", 15, 12,
                    List.of(new ItemSeed("latitude", 80, "8"), new ItemSeed("monitor", 80, "10")), "WON", null, false),
            new OpportunitySeed("banco-soc", "SOC gerenciado 24x7", "banco-sul", 1, "ana", 60, 36,
                    List.of(new ItemSeed("soc", 1, "12"), new ItemSeed("forticlient", 3000, "10")), "PROPOSAL", null, false),
            new OpportunitySeed("banco-servers", "Expansao de servidores de core bancario", "banco-sul", 2, "ana", 90, 12,
                    List.of(new ItemSeed("dl380", 6, "0"), new ItemSeed("rhel", 12, "0")), "QUALIFICATION", null, false),
            new OpportunitySeed("horizonte-erp", "Implantacao do Protheus", "horizonte", 0, "joao", 75, 24,
                    List.of(new ItemSeed("erp-rollout", 1, "5"), new ItemSeed("protheus", 120, "8")), "PROPOSAL", null, false),
            new OpportunitySeed("horizonte-desktops", "Desktops para a fabrica", "horizonte", 1, "joao", 10, 12,
                    List.of(new ItemSeed("optiplex", 60, "12"), new ItemSeed("win11", 60, "0")), "WON", null, true),
            new OpportunitySeed("veloz-backup", "Backup gerenciado", "veloz", 0, "bruno", 25, 24,
                    List.of(new ItemSeed("backup-managed", 1, "0"), new ItemSeed("veeam", 2, "0")), "LOST", "cliente optou por solucao do provedor de nuvem atual", false),
            new OpportunitySeed("novo-saber-lab", "Laboratorio de informatica", "novo-saber", 1, "camila", 35, 12,
                    List.of(new ItemSeed("optiplex", 40, "5"), new ItemSeed("monitor", 40, "5")), "QUALIFICATION", null, false),
            new OpportunitySeed("campo-verde-rede", "Rede da prefeitura (pregao eletronico)", "campo-verde", 0, "larissa", 80, 12,
                    List.of(new ItemSeed("catalyst", 25, "8"), new ItemSeed("fortigate", 4, "8")), "PROSPECTING", null, false),
            new OpportunitySeed("agrovale-cloud", "Migracao para nuvem", "agrovale", 0, "joao", 50, 12,
                    List.of(new ItemSeed("cloud-consulting", 320, "10")), "PROSPECTING", null, false),
            new OpportunitySeed("solar-github", "Plataforma de desenvolvimento", "solar", 1, "camila", 40, 12,
                    List.of(new ItemSeed("github", 45, "5")), "PROPOSAL", null, false),
            new OpportunitySeed("conecta-soc", "Monitoramento NOC", "conecta", 0, "bruno", 18, 24,
                    List.of(new ItemSeed("soc", 1, "8"), new ItemSeed("support-247", 1, "10")), "WON", null, false),
            new OpportunitySeed("moraes-m365", "Microsoft 365 e seguranca", "moraes-adv", 0, "larissa", 12, 12,
                    List.of(new ItemSeed("m365-e3", 45, "8"), new ItemSeed("kaspersky", 45, "12")), "LOST", "preco acima do concorrente", false),
            new OpportunitySeed("estrela-crm", "CRM para a equipe de vendas", "estrela", 0, "camila", 28, 12,
                    List.of(new ItemSeed("rdstation", 35, "10")), "QUALIFICATION", null, false),
            new OpportunitySeed("vida-plena-notebooks", "Notebooks para consultorios", "vida-plena", 0, "bruno", 22, 12,
                    List.of(new ItemSeed("thinkpad", 15, "14")), "NEGOTIATION", null, false),
            new OpportunitySeed("alicerce-retorno", "Retomada: suporte e backup", "alicerce", 0, "ana", 55, 24,
                    List.of(new ItemSeed("support-247", 1, "5"), new ItemSeed("backup-managed", 1, "5")), "PROSPECTING", null, false)
    );

    public static final List<ActivitySeed> ACTIVITIES = List.of(
            new ActivitySeed("MEETING", "Apresentacao da arquitetura do data center", "OPPORTUNITY", "hsc-infra", "ana", 26, 90, "Sede do Hospital Santa Clara", null, null),
            new ActivitySeed("CALL", "Alinhar prazo de entrega dos servidores", "OPPORTUNITY", "hsc-infra", "felipe", -20, 0, null, "Cliente aceitou entrega em duas fases", 18),
            new ActivitySeed("TASK", "Enviar proposta revisada com desconto", "OPPORTUNITY", "farma-wifi", "joao", -6, 0, null, null, null),
            new ActivitySeed("MEETING", "Site survey Wi-Fi na loja piloto", "OPPORTUNITY", "farma-wifi", "felipe", 50, 180, "Loja Farma Bem Paulista", null, null),
            new ActivitySeed("EMAIL", "Enviar escopo do SOC para a seguranca", "OPPORTUNITY", "banco-soc", "ana", 4, 0, null, null, null),
            new ActivitySeed("MEETING", "Workshop de requisitos do SOC", "OPPORTUNITY", "banco-soc", "ana", 74, 120, "https://teams.microsoft.com/pbtech-banco-sul", null, null),
            new ActivitySeed("CALL", "Qualificar volume de servidores", "OPPORTUNITY", "banco-servers", "ana", -48, 0, null, "Confirmada demanda de 6 servidores no 1o trimestre", 25),
            new ActivitySeed("TASK", "Levantar modulos do Protheus necessarios", "OPPORTUNITY", "horizonte-erp", "joao", 30, 0, null, null, null),
            new ActivitySeed("MEETING", "Demonstracao do Protheus", "OPPORTUNITY", "horizonte-erp", "felipe", 98, 120, "Horizonte - sala de reunioes", null, null),
            new ActivitySeed("CALL", "Entender motivo da perda do backup", "OPPORTUNITY", "veloz-backup", "bruno", -30, 0, null, "Contrato fechado com o provedor de nuvem por 12 meses", 12),
            new ActivitySeed("TASK", "Preparar planilha de configuracao do laboratorio", "OPPORTUNITY", "novo-saber-lab", "camila", -3, 0, null, null, null),
            new ActivitySeed("EMAIL", "Pedir edital do pregao", "OPPORTUNITY", "campo-verde-rede", "larissa", 8, 0, null, null, null),
            new ActivitySeed("CALL", "Descoberta: ambiente atual da AgroVale", "OPPORTUNITY", "agrovale-cloud", "joao", 20, 0, null, null, null),
            new ActivitySeed("MEETING", "PoC do GitHub Enterprise", "OPPORTUNITY", "solar-github", "felipe", 122, 60, "https://meet.google.com/pbtech-solar", null, null),
            new ActivitySeed("TASK", "Validar SLA do NOC com juridico", "OPPORTUNITY", "conecta-soc", "bruno", -72, 0, null, "SLA aprovado", null),
            new ActivitySeed("CALL", "Negociar desconto de notebooks", "OPPORTUNITY", "vida-plena-notebooks", "bruno", -2, 0, null, null, null),
            new ActivitySeed("TASK", "Mapear usuarios do CRM", "OPPORTUNITY", "estrela-crm", "camila", 44, 0, null, null, null),
            new ActivitySeed("CALL", "Retomar contato com ex-cliente", "OPPORTUNITY", "alicerce-retorno", "ana", 6, 0, null, null, null),
            new ActivitySeed("MEETING", "Reuniao trimestral de relacionamento", "COMPANY", "santa-clara", "larissa", 170, 60, "Hospital Santa Clara", null, null),
            new ActivitySeed("TASK", "Atualizar cadastro de contatos", "COMPANY", "pixel", "larissa", 12, 0, null, null, null),
            new ActivitySeed("EMAIL", "Enviar portfolio de parceiros", "COMPANY", "pixel", "larissa", -26, 0, null, "Portfolio enviado", null),
            new ActivitySeed("CALL", "Check-in pos-venda", "COMPANY", "conecta", "bruno", 30, 0, null, null, null),
            new ActivitySeed("TASK", "Registrar oportunidade de expansao", "COMPANY", "agrovale", "joao", -10, 0, null, null, null),
            new ActivitySeed("CALL", "Primeiro contato", "LEAD", "Marcelo Tavares", "ana", 3, 0, null, null, null),
            new ActivitySeed("CALL", "Primeiro contato", "LEAD", "Anderson Faria", "joao", -5, 0, null, null, null),
            new ActivitySeed("EMAIL", "Enviar material institucional", "LEAD", "Daniela Prado", "camila", 10, 0, null, null, null),
            new ActivitySeed("MEETING", "Reuniao de qualificacao", "LEAD", "Roberto Menezes", "ana", 52, 45, "https://teams.microsoft.com/pbtech-usina", null, null),
            new ActivitySeed("CALL", "Follow-up da proposta", "LEAD", "Cesar Brandao", "larissa", -1, 0, null, null, null),
            new ActivitySeed("TASK", "Pesquisar parque tecnologico do Grupo Atlas", "LEAD", "Mauricio Sales", "ana", 16, 0, null, null, null),
            new ActivitySeed("CALL", "Validar orcamento", "LEAD", "Viviane Castro", "bruno", 28, 0, null, "Orcamento aprovado para o 2o semestre", 20)
    );

    public static BigDecimal decimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
