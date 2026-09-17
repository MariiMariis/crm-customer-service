"use client";

import { useEffect, useState } from "react";
import { getAgents, createAgent, deleteAgent } from "@/lib/api";

const EMPTY_FORM = { name: "", email: "", department: "", active: true };

export default function AgentsPage() {
    const [agents, setAgents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [form, setForm] = useState(EMPTY_FORM);
    const [submitting, setSubmitting] = useState(false);

    const loadAgents = () => {
        setLoading(true);
        getAgents()
            .then(setAgents)
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    };

    useEffect(loadAgents, []);

    const handleChange = (field) => (e) =>
        setForm((prev) => ({ ...prev, [field]: e.target.value }));

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setError(null);
        try {
            await createAgent(form);
            setForm(EMPTY_FORM);
            loadAgents();
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    const handleDelete = async (id) => {
        if (!confirm("Remover este atendente?")) return;
        try {
            await deleteAgent(id);
            loadAgents();
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <>
            <h1>Atendentes</h1>
            <p className="page-subtitle">Equipe responsavel por tratar os tickets</p>

            {error && <div className="error-banner">{error}</div>}

            <div className="card">
                <h2>Novo atendente</h2>
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
                        <div className="form-field">
                            <label>Departamento</label>
                            <input value={form.department} onChange={handleChange("department")} />
                        </div>
                    </div>
                    <button type="submit" disabled={submitting}>
                        {submitting ? "Salvando..." : "Adicionar atendente"}
                    </button>
                </form>
            </div>

            <div className="card">
                <h2>Atendentes cadastrados</h2>
                {loading ? (
                    <p>Carregando...</p>
                ) : agents.length === 0 ? (
                    <p className="empty-state">Nenhum atendente cadastrado ainda.</p>
                ) : (
                    <table>
                        <thead>
                            <tr>
                                <th>Nome</th>
                                <th>Email</th>
                                <th>Departamento</th>
                                <th>Status</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {agents.map((a) => (
                                <tr key={a.id}>
                                    <td>{a.name}</td>
                                    <td>{a.email}</td>
                                    <td>{a.department || "-"}</td>
                                    <td>{a.active ? "Ativo" : "Inativo"}</td>
                                    <td>
                                        <button className="secondary" onClick={() => handleDelete(a.id)}>
                                            Remover
                                        </button>
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
