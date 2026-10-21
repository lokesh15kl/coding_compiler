import { useEffect, useState } from 'react';
import api from '../api/axios';
import { loadDevHistory } from '../auth/devCompiler';

type HistoryItem = {
  id: string | number;
  title: string;
  language: string;
  status: string;
  executionTime: number;
  memoryUsage: number;
  createdAt: string;
};

export default function HistoryPage() {
  const [items, setItems] = useState<HistoryItem[]>([]);

  useEffect(() => {
    api.get('/api/history')
      .then(({ data }) => setItems(data))
      .catch(() => {
        setItems(loadDevHistory());
      });
  }, []);

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-2xl font-semibold">Program history</h2>
        <p className="mt-1 text-sm text-slate-400">Execution status, timing, and timestamps for recent runs.</p>
      </div>

      <div className="overflow-hidden rounded-3xl border border-slate-800 bg-slate-950/70">
        <table className="min-w-full divide-y divide-slate-800 text-left text-sm">
          <thead className="bg-slate-950/90 text-slate-300">
            <tr>
              <th className="px-5 py-4 font-medium">Title</th>
              <th className="px-5 py-4 font-medium">Language</th>
              <th className="px-5 py-4 font-medium">Execution Time</th>
              <th className="px-5 py-4 font-medium">Status</th>
              <th className="px-5 py-4 font-medium">Date</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800 text-slate-200">
            {items.map((item) => (
              <tr key={item.id} className="hover:bg-slate-900/60">
                <td className="px-5 py-4">{item.title}</td>
                <td className="px-5 py-4 capitalize">{item.language}</td>
                <td className="px-5 py-4">{item.executionTime} ms</td>
                <td className="px-5 py-4">
                  <span className="rounded-full border border-slate-700 px-3 py-1 text-xs uppercase tracking-[0.18em] text-cyan-200">
                    {item.status}
                  </span>
                </td>
                <td className="px-5 py-4">{new Date(item.createdAt).toLocaleString()}</td>
              </tr>
            ))}
            {!items.length ? (
              <tr>
                <td className="px-5 py-8 text-slate-400" colSpan={5}>
                  No execution history yet.
                </td>
              </tr>
            ) : null}
          </tbody>
        </table>
      </div>
    </div>
  );
}