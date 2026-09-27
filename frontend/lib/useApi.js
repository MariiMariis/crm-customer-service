"use client";

import { useCallback, useEffect, useRef, useState } from "react";

export function useApi(loader, dependencies = []) {
    const [data, setData] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const loaderRef = useRef(loader);
    loaderRef.current = loader;

    const reload = useCallback(async () => {
        setLoading(true);
        try {
            const result = await loaderRef.current();
            setData(result);
            setError(null);
            return result;
        } catch (err) {
            setError(err);
            return null;
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        reload();
    }, dependencies);

    return { data, error, loading, reload, setData };
}

export function useDebounced(value, delay = 300) {
    const [debounced, setDebounced] = useState(value);
    useEffect(() => {
        const timer = setTimeout(() => setDebounced(value), delay);
        return () => clearTimeout(timer);
    }, [value, delay]);
    return debounced;
}
