import { Inter } from "next/font/google";
import AppShell from "@/components/layout/AppShell";
import "./globals.css";

const inter = Inter({ subsets: ["latin"], display: "swap" });

export const metadata = {
    title: "Nexo · CRM",
    description: "Nexo: cada negócio, conectado. CRM B2B para vendas de software, hardware e serviços de TI",
};

export default function RootLayout({ children }) {
    return (
        <html lang="pt-BR" className={inter.className}>
            <body>
                <AppShell>{children}</AppShell>
            </body>
        </html>
    );
}
