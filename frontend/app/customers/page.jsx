"use client";

import { useEffect, useState } from "react";
import { getCustomers, createCustomer, deleteCustomer } from "@/lib/api";
import NotificationPreferences from "@/components/NotificationPreferences";

const EMPTY_FORM = { name: "", email: "", phone: "", document: "" };

export default function CustomersPage() {
    const [customers, setCustomers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [form, setForm] = useState(EMPTY_FORM);
    const [submitting, setSubmitting] = useState(false);
    const [preferencesFor, setPreferencesFor] = useState(null);

    const loadCustomers = () => {
        setLoading(true);
        getCustomers()
            .then(setCustomers)
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    };

    useEffect(loadCustomers, []);

    const handleChange = (field) => (e) =>
        setForm((prev) => ({ ...prev, [field]: e.target.value }));

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            await createCustomer(form);
            setForm(EMPTY_FORM);
            loadCustomers();
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async (id) => {
        if (!confirm("Remover este cliente?")) return;
        try {
            await deleteCustomer(id);
            loadCustomers();
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <>
            <h1>Clientes</h1>
            <p className="page-subtitle">Cadastro de clientes atendidos</p>

            {error && <div className="error-banner">{error}</div>}

            {preferencesFor && (
                <NotificationPreferences customer={preferencesFor} onClose={() => setPreferencesFor(null)} />
            )}

            <div className="card">
                <h2>Novo cliente</h2>
                <form onSubmit={handleSubmit}>
                    <div className="form-row">
                        <div className="form-field">
                            <label>Nome</label>
                            <input required value={form.name} onChange={handleChange("name")} />
                        </div>
                        <div className="form-field">
                            <label>Email</label>
                            <input required type="email" value={form.email} onChange={handleChange("email")} />
                        </div>
                    </div>
                    <div className="form-row">
                        <div className="form-field">
                            <label>Telefone</label>
                            <input value={form.phone} onChange={handleChange("phone")} />
                        </div>
                        <div className="form-field">
                            <label>Documento</label>
                            <input value={form.document} onChange={handleChange("document")} />
                        </div>
                    </div>
                    <button type="submit" disabled={submitting}>
                        {submitting ? "Salvando..." : "Adicionar cliente"}
                    </button>
                </form>
            </div>

            <div className="card">
                <h2>Clientes cadastrados</h2>
                {loading ? (
                    <p>Carregando...</p>
                ) : customers.length === 0 ? (
                    <p className="empty-state">Nenhum cliente cadastrado ainda.</p>
                ) : (
                    <table>
                        <thead>
                            <tr>
                                <th>Nome</th>
                                <th>Email</th>
                                <th>Telefone</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {customers.map((c) => (
                                <tr key={c.id}>
                                    <td>{c.name}</td>
                                    <td>{c.email}</td>
                                    <td>{c.phone || "-"}</td>
                                    <td>
                                        <div className="actions-row">
                                            <button className="secondary" onClick={() => setPreferencesFor(c)}>
                                                Notificacoes
                                            </button>
                                            <button className="secondary" onClick={() => handleDelete(c.id)}>
                                                Remover
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </>
    );
}
