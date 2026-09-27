"use client";

import { CurrentUserProvider } from "@/components/providers/CurrentUserProvider";
import { ToastProvider } from "@/components/providers/ToastProvider";
import Sidebar from "@/components/layout/Sidebar";
import Topbar from "@/components/layout/Topbar";

export default function AppShell({ children }) {
    return (
        <ToastProvider>
            <CurrentUserProvider>
                <Sidebar />
                <div className="lg:pl-60">
                    <Topbar />
                    <main className="mx-auto max-w-[1400px] px-4 py-8 sm:px-8">{children}</main>
                </div>
            </CurrentUserProvider>
        </ToastProvider>
    );
}
