"use client";

import { useEffect, useState } from "react";
import { useApi } from "@/lib/useApi";

export function usePolling(loader, intervalMs, paused, dependencies = []) {
    const state = useApi(loader, dependencies);
    const [updatedAt, setUpdatedAt] = useState(null);
    const { reload, data, error } = state;

    useEffect(() => {
        if (data || error) setUpdatedAt(new Date());
    }, [data, error]);

    useEffect(() => {
        if (paused) return undefined;
        const timer = setInterval(reload, intervalMs);
        return () => clearInterval(timer);
    }, [paused, intervalMs, reload]);

    return { ...state, updatedAt };
}
