"use client";

import { Search } from "lucide-react";

export default function SearchInput({ value, onChange, placeholder = "Buscar..." }) {
    return (
        <div className="relative w-full sm:w-72">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
            <input
                className="input pl-9"
                value={value}
                placeholder={placeholder}
                onChange={(event) => onChange(event.target.value)}
            />
        </div>
    );
}
