export function Field({ label, hint, className = "", children }) {
    return (
        <label className={`block ${className}`}>
            {label && <span className="label">{label}</span>}
            {children}
            {hint && <span className="mt-1 block text-xs text-slate-500">{hint}</span>}
        </label>
    );
}

export function Input({ label, hint, className = "", ...props }) {
    return (
        <Field label={label} hint={hint} className={className}>
            <input className="input" {...props} />
        </Field>
    );
}

export function Textarea({ label, hint, className = "", rows = 3, ...props }) {
    return (
        <Field label={label} hint={hint} className={className}>
            <textarea className="input resize-y" rows={rows} {...props} />
        </Field>
    );
}

export function Select({ label, hint, options = [], placeholder, className = "", ...props }) {
    return (
        <Field label={label} hint={hint} className={className}>
            <select className="input" {...props}>
                {placeholder !== undefined && <option value="">{placeholder}</option>}
                {options.map((option) => (
                    <option key={option.value} value={option.value}>
                        {option.label}
                    </option>
                ))}
            </select>
        </Field>
    );
}
