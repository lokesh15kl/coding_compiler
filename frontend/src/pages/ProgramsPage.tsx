import { useEffect, useState } from 'react';
import api from '../api/axios';

type Program = {
  id: number;
  title: string;
  language: string;
  updatedAt: string;
  shareId?: string;
};

export default function ProgramsPage() {
  const [items, setItems] = useState<Program[]>([]);

  useEffect(() => {
    api.get('/api/program/my').then(({ data }) => setItems(data)).catch(() => setItems([]));
  }, []);

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-2xl font-semibold">Saved programs</h2>
        <p className="mt-1 text-sm text-slate-400">Titles, languages, and updates are listed here.</p>
      </div>
      <div className="grid gap-4">
        {items.map((item) => (
          <article key={item.id} className="rounded-3xl border border-slate-800 bg-slate-950/70 p-5">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div>
                <h3 className="text-lg font-semibold">{item.title}</h3>
                <p className="text-sm text-slate-400">{item.language} • Updated {new Date(item.updatedAt).toLocaleString()}</p>
              </div>
              {item.shareId ? <a className="btn-secondary" href={`/share/${item.shareId}`}>Open share link</a> : null}
            </div>
          </article>
        ))}
        {!items.length ? <div className="rounded-3xl border border-slate-800 bg-slate-950/70 p-5 text-sm text-slate-400">No saved programs found.</div> : null}
      </div>
    </div>
  );
}