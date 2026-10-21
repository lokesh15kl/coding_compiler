import { useEffect, useState } from 'react';
import api from '../api/axios';

type Stats = {
  totalPrograms: number;
  totalExecutions: number;
  successfulExecutions: number;
  favoriteLanguage: string;
};

export default function DashboardPage() {
  const [stats, setStats] = useState<Stats>({
    totalPrograms: 0,
    totalExecutions: 0,
    successfulExecutions: 0,
    favoriteLanguage: 'Java'
  });

  useEffect(() => {
    api.get('/api/dashboard').then(({ data }) => setStats(data)).catch(() => undefined);
  }, []);

  const cards = [
    { label: 'Programs Saved', value: stats.totalPrograms },
    { label: 'Executions', value: stats.totalExecutions },
    { label: 'Successful Runs', value: stats.successfulExecutions },
    { label: 'Favorite Language', value: stats.favoriteLanguage }
  ];

  return (
    <div className="space-y-6">
      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {cards.map((card) => (
          <article key={card.label} className="rounded-3xl border border-slate-800 bg-slate-950/70 p-5">
            <p className="text-sm text-slate-400">{card.label}</p>
            <div className="mt-3 text-3xl font-semibold text-white">{card.value}</div>
          </article>
        ))}
      </section>

      <section className="grid gap-4 xl:grid-cols-2">
        <article className="rounded-3xl border border-slate-800 bg-slate-950/70 p-5">
          <h2 className="text-lg font-semibold">Recent activity</h2>
          <p className="mt-2 text-sm text-slate-400">Your latest programs and execution history appear here.</p>
          <div className="mt-4 space-y-3 text-sm text-slate-300">
            <div className="rounded-2xl border border-slate-800 px-4 py-3">No recent activity loaded yet.</div>
          </div>
        </article>
        <article className="rounded-3xl border border-slate-800 bg-slate-950/70 p-5">
          <h2 className="text-lg font-semibold">Overview</h2>
          <p className="mt-2 text-sm text-slate-400">Use the compiler page to run code in isolated Docker containers.</p>
          <div className="mt-4 rounded-2xl bg-cyan-500/10 p-4 text-sm text-cyan-200">
            Professional tooling with JWT auth, history tracking, and program saving.
          </div>
        </article>
      </section>
    </div>
  );
}