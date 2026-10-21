type Props = {
  title: string;
  executionTime?: number;
  memoryUsage?: number;
  output: string;
  stderr?: string;
  status?: string;
};

export default function OutputPanel({ title, executionTime, memoryUsage, output, stderr, status }: Props) {
  return (
    <section className="output-panel glass-card rounded-3xl p-5">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-lg font-semibold">{title}</h2>
          <p className="text-sm text-slate-400">Compilation output, runtime output, and metrics</p>
        </div>
        <div className="flex gap-2 text-xs text-slate-300">
          {status && <span className="rounded-full border border-slate-700 px-3 py-1">{status}</span>}
          {typeof executionTime === 'number' && <span className="rounded-full border border-slate-700 px-3 py-1">{executionTime} ms</span>}
          {typeof memoryUsage === 'number' && <span className="rounded-full border border-slate-700 px-3 py-1">{memoryUsage} KB</span>}
        </div>
      </div>
      <div className="space-y-4 text-sm">
        <pre className="output-box max-h-64 overflow-auto rounded-2xl border border-slate-800 bg-slate-950/80 p-4 text-slate-100">{output || 'No output yet.'}</pre>
        {stderr ? <pre className="max-h-64 overflow-auto rounded-2xl border border-rose-900/50 bg-rose-950/30 p-4 text-rose-200">{stderr}</pre> : null}
      </div>
    </section>
  );
}