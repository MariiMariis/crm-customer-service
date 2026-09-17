import NavBar from "@/components/NavBar";
import "./globals.css";

export const metadata = {
    title: "PB CRM - Customer Service",
    description: "CRM de atendimento ao cliente",
};

export default function RootLayout({ children }) {
    return (
        <html lang="pt-BR">
            <body>
                <NavBar />
                <main className="page-container">{children}</main>
            </body>
        </html>
    );
}
