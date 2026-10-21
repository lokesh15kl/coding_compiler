import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../api/axios';
import CodeEditor from '../components/CodeEditor';

type SharedProgram = {
  title: string;
  language: string;
  code: string;
};

export default function SharedProgramPage() {
  const { shareId } = useParams();
  const [program, setProgram] = useState<SharedProgram | null>(null);

  useEffect(() => {
    if (!shareId) {
      return;
    }
    api.get(`/api/program/share/${shareId}`).then(({ data }) => setProgram(data)).catch(() => setProgram(null));
  }, [shareId]);

  if (!program) {
    return <div className="grid min-h-screen place-items-center text-slate-300">Shared program not available.</div>;
  }

  return (
    <div className="min-h-screen p-6 text-slate-100">
      <div className="mx-auto max-w-6xl space-y-4">
        <div>
          <p className="text-xs uppercase tracking-[0.28em] text-cyan-300">Shared Program</p>
          <h1 className="mt-2 text-3xl font-semibold">{program.title}</h1>
        </div>
        <CodeEditor language={program.language} code={program.code} onChange={() => undefined} />
      </div>
    </div>
  );
}