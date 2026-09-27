"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { api, CURRENT_USER_KEY } from "@/lib/api";

const CurrentUserContext = createContext(null);

export function CurrentUserProvider({ children }) {
    const [salesReps, setSalesReps] = useState([]);
    const [user, setUser] = useState(null);
    const [error, setError] = useState(null);

    const loadSalesReps = useCallback(async () => {
        try {
            const page = await api.salesReps.list({ pageSize: 100 });
            setSalesReps(page.content);
            setError(null);
            const stored = JSON.parse(window.localStorage.getItem(CURRENT_USER_KEY) ?? "null");
            const match = page.content.find((rep) => rep.id === stored?.id) ?? page.content.find((rep) => rep.role === "MANAGER");
            setUser(match ?? null);
            if (match) window.localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(match));
        } catch (err) {
            setError(err);
        }
    }, []);

    useEffect(() => {
        loadSalesReps();
    }, [loadSalesReps]);

    const selectUser = useCallback(
        (id) => {
            const next = salesReps.find((rep) => rep.id === Number(id)) ?? null;
            setUser(next);
            if (next) window.localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(next));
        },
        [salesReps]
    );

    const value = useMemo(() => {
        const subordinates = user ? salesReps.filter((rep) => rep.managerId === user.id) : [];
        return {
            user,
            salesReps,
            activeSalesReps: salesReps.filter((rep) => rep.active),
            subordinates,
            isManager: user?.role === "MANAGER",
            error,
            selectUser,
            reload: loadSalesReps,
            nameOf: (id) => salesReps.find((rep) => rep.id === id)?.name ?? null,
        };
    }, [user, salesReps, error, selectUser, loadSalesReps]);

    return <CurrentUserContext.Provider value={value}>{children}</CurrentUserContext.Provider>;
}

export function useCurrentUser() {
    return useContext(CurrentUserContext);
}
