"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const LINKS = [
    { href: "/", label: "Dashboard" },
    { href: "/customers", label: "Clientes" },
    { href: "/agents", label: "Atendentes" },
    { href: "/tickets", label: "Tickets" },
];

export default function NavBar() {
    const pathname = usePathname();

    return (
        <header className="navbar">
            <div className="navbar-brand">PB CRM · Customer Service</div>
            <nav className="navbar-links">
                {LINKS.map((link) => (
                    <Link
                        key={link.href}
                        href={link.href}
                        className={pathname === link.href ? "active" : ""}
                    >
                        {link.label}
                    </Link>
                ))}
            </nav>
        </header>
    );
}
