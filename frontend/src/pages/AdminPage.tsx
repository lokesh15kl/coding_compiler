import { FormEvent, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import api from '../api/axios';

type AdminStats = {
  totalUsers: number;
  totalPrograms: number;
  totalExecutions: number;
  mostUsedLanguage: string;
};

type TopicItem = {
  id: number;
  name: string;
  description: string;
};

type ProblemForm = {
  topicId: string;
  title: string;
  difficulty: string;
  language: string;
  statement: string;
  inputFormat: string;
  outputFormat: string;
  sampleInput: string;
  sampleOutput: string;
};

type ProblemItem = ProblemForm & {
  id: number;
  createdAt?: string;
  updatedAt?: string;
};

type UserPerformance = {
  userId: number;
  name: string;
  email: string;
  totalRuns: number;
  successfulRuns: number;
  failedRuns: number;
  successRate: number;
  bestExecutionTimeMs: number;
};

type RoadmapProgressSummary = {
  userId: number;
  name: string;
  email: string;
  totalQuestions: number;
  completedQuestions: number;
  totalAttempts: number;
  completionRate: number;
};

type RoadmapQuestion = {
  id: string;
  topic: string;
  title: string;
  difficulty: string;
  prompt: string;
  tags: string[];
};

type RoadmapTopic = {
  topic: string;
  description: string;
  questions: RoadmapQuestion[];
};

const emptyForm: ProblemForm = {
  topicId: '',
  title: '',
  difficulty: 'Easy',
  language: 'Java',
  statement: '',
  inputFormat: '',
  outputFormat: '',
  sampleInput: '',
  sampleOutput: ''
};

export default function AdminPage() {
  const [stats, setStats] = useState<AdminStats>({
    totalUsers: 0,
    totalPrograms: 0,
    totalExecutions: 0,
    mostUsedLanguage: 'Java'
  });
  const [topics, setTopics] = useState<TopicItem[]>([]);
  const [problems, setProblems] = useState<ProblemItem[]>([]);
  const [performance, setPerformance] = useState<UserPerformance[]>([]);
  const [roadmapProgress, setRoadmapProgress] = useState<RoadmapProgressSummary[]>([]);
  const [roadmapText, setRoadmapText] = useState('');
  const [roadmapTopics, setRoadmapTopics] = useState<RoadmapTopic[]>([]);
  const [form, setForm] = useState<ProblemForm>(emptyForm);
  const [topicName, setTopicName] = useState('');
  const [topicDescription, setTopicDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [topicLoading, setTopicLoading] = useState(false);

  const loadAdminData = async () => {
    try {
      const [statsResponse, topicsResponse, problemsResponse, performanceResponse, roadmapProgressResponse] = await Promise.all([
        api.get('/api/admin/stats'),
        api.get('/api/admin/topics'),
        api.get('/api/admin/problems'),
        api.get('/api/admin/performance'),
        api.get('/api/admin/roadmap/progress')
      ]);
      setStats(statsResponse.data);
      setTopics(topicsResponse.data);
      setProblems(problemsResponse.data);
      setPerformance(performanceResponse.data);
      setRoadmapProgress(roadmapProgressResponse.data);

      const roadmapResponse = await api.get('/api/roadmap');
      setRoadmapTopics(roadmapResponse.data);
      setRoadmapText(
        JSON.stringify(
          roadmapResponse.data,
          null,
          2
        )
      );
    } catch (error) {
      toast.error('Unable to load admin data');
    }
  };

  useEffect(() => {
    void loadAdminData();
  }, []);

  const submitProblem = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      const payload = {
        ...form,
        topicId: form.topicId ? Number(form.topicId) : null
      };
      await api.post('/api/admin/problems', payload);
      setForm(emptyForm);
      await loadAdminData();
      toast.success('Problem added successfully');
    } catch (error) {
      toast.error('Unable to add problem');
    } finally {
      setLoading(false);
    }
  };

  const submitTopic = async (event: FormEvent) => {
    event.preventDefault();
    if (!topicName.trim()) {
      toast.error('Topic name is required');
      return;
    }
    setTopicLoading(true);
    try {
      await api.post('/api/admin/topics', { name: topicName, description: topicDescription });
      setTopicName('');
      setTopicDescription('');
      await loadAdminData();
      toast.success('Topic created');
    } catch (error) {
      toast.error('Unable to create topic');
    } finally {
      setTopicLoading(false);
    }
  };

  const deleteProblem = async (problemId: number) => {
    try {
      await api.delete(`/api/admin/problems/${problemId}`);
      await loadAdminData();
      toast.success('Problem deleted');
    } catch (error) {
      toast.error('Unable to delete problem');
    }
  };

  const submitRoadmap = async (event: FormEvent) => {
    event.preventDefault();
    try {
      const parsed = JSON.parse(roadmapText) as RoadmapTopic[];
      await api.post('/api/admin/roadmap', {
        name: 'Roadmap',
        topics: parsed
      });
      await loadAdminData();
      toast.success('Roadmap pushed successfully');
    } catch (error) {
      toast.error('Invalid roadmap JSON. Use the expected topic/question structure.');
    }
  };

  const resetRoadmapToDefault = async () => {
    try {
      await api.post('/api/admin/roadmap/reset');
      await loadAdminData();
      toast.success('Roadmap reset to default 100-question path');
    } catch (error) {
      toast.error('Unable to reset roadmap');
    }
  };

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-2xl font-semibold">Admin Panel</h2>
        <p className="mt-1 text-sm text-slate-400">Manage coding challenges and review user performance.</p>
      </div>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {Object.entries(stats).map(([label, value]) => (
          <article key={label} className="rounded-3xl border border-slate-800 bg-slate-950/70 p-5">
            <p className="text-sm text-slate-400">{label}</p>
            <div className="mt-3 text-3xl font-semibold text-white">{String(value)}</div>
          </article>
        ))}
      </section>

      <section className="grid gap-6 xl:grid-cols-[1.1fr,0.9fr]">
        <div className="space-y-6">
          <form className="rounded-3xl border border-slate-800 bg-slate-950/70 p-5" onSubmit={submitTopic}>
            <h3 className="text-xl font-semibold">Add Topic</h3>
            <div className="mt-4 space-y-3">
              <input className="field" value={topicName} onChange={(event) => setTopicName(event.target.value)} placeholder="Topic name" />
              <textarea className="field min-h-20" value={topicDescription} onChange={(event) => setTopicDescription(event.target.value)} placeholder="Topic description" />
              <button className="btn-primary w-full" disabled={topicLoading} type="submit">
                {topicLoading ? 'Saving...' : 'Create topic'}
              </button>
            </div>
          </form>

          <div className="rounded-3xl border border-slate-800 bg-slate-950/70 p-5">
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-xl font-semibold">Topics</h3>
              <span className="rounded-full border border-cyan-500/40 bg-cyan-500/10 px-3 py-1 text-xs uppercase tracking-[0.2em] text-cyan-200">
                {topics.length} total
              </span>
            </div>
            <div className="space-y-2">
              {topics.length === 0 ? (
                <p className="text-sm text-slate-400">No topics added yet.</p>
              ) : (
                topics.map((topic) => (
                  <div key={topic.id} className="rounded-2xl border border-slate-800 bg-slate-900/80 px-4 py-3 text-sm text-slate-200">
                    <div className="font-medium text-white">{topic.name}</div>
                    <div className="mt-1 text-slate-400">{topic.description || 'No description provided.'}</div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

        <div className="rounded-3xl border border-slate-800 bg-slate-950/70 p-5">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="text-xl font-semibold">List of Problems</h3>
            <span className="rounded-full border border-cyan-500/40 bg-cyan-500/10 px-3 py-1 text-xs uppercase tracking-[0.2em] text-cyan-200">
              {problems.length} total
            </span>
          </div>

          <div className="space-y-3">
            {problems.length === 0 ? (
              <p className="text-sm text-slate-400">No problems added yet.</p>
            ) : (
              problems.map((problem) => (
                <div key={problem.id} className="rounded-2xl border border-slate-800 bg-slate-900/90 p-4">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <div className="font-medium text-white">{problem.title}</div>
                      <div className="mt-1 text-xs uppercase tracking-[0.2em] text-cyan-300">
                        {problem.difficulty} • {problem.language}
                      </div>
                    </div>
                    <button className="btn-secondary text-xs" type="button" onClick={() => deleteProblem(problem.id)}>
                      Delete
                    </button>
                  </div>
                  <p className="mt-3 line-clamp-3 text-sm text-slate-300">{problem.statement}</p>
                </div>
              ))
            )}
          </div>
        </div>

        <form className="rounded-3xl border border-slate-800 bg-slate-950/70 p-5" onSubmit={submitProblem}>
          <h3 className="text-xl font-semibold">Add Problem</h3>
          <div className="mt-4 space-y-3">
            <select className="field" value={form.topicId} onChange={(event) => setForm({ ...form, topicId: event.target.value })}>
              <option value="">Select topic (optional)</option>
              {topics.map((topic) => (
                <option key={topic.id} value={String(topic.id)}>{topic.name}</option>
              ))}
            </select>
            <input
              className="field"
              value={form.title}
              onChange={(event) => setForm({ ...form, title: event.target.value })}
              placeholder="Problem title"
            />
            <div className="grid gap-3 sm:grid-cols-2">
              <select className="field" value={form.difficulty} onChange={(event) => setForm({ ...form, difficulty: event.target.value })}>
                <option>Easy</option>
                <option>Medium</option>
                <option>Hard</option>
              </select>
              <select className="field" value={form.language} onChange={(event) => setForm({ ...form, language: event.target.value })}>
                <option>Java</option>
                <option>Python</option>
                <option>C</option>
                <option>C++</option>
                <option>JavaScript</option>
              </select>
            </div>
            <textarea className="field min-h-28" value={form.statement} onChange={(event) => setForm({ ...form, statement: event.target.value })} placeholder="Problem statement" />
            <textarea className="field min-h-20" value={form.inputFormat} onChange={(event) => setForm({ ...form, inputFormat: event.target.value })} placeholder="Input format" />
            <textarea className="field min-h-20" value={form.outputFormat} onChange={(event) => setForm({ ...form, outputFormat: event.target.value })} placeholder="Output format" />
            <textarea className="field min-h-20" value={form.sampleInput} onChange={(event) => setForm({ ...form, sampleInput: event.target.value })} placeholder="Sample input" />
            <textarea className="field min-h-20" value={form.sampleOutput} onChange={(event) => setForm({ ...form, sampleOutput: event.target.value })} placeholder="Sample output" />
            <button className="btn-primary w-full" disabled={loading} type="submit">
              {loading ? 'Saving...' : 'Create problem'}
            </button>
          </div>
        </form>
      </section>

      <section className="rounded-3xl border border-slate-800 bg-slate-950/70 p-5">
        <div className="mb-4 flex items-center justify-between gap-3">
          <h3 className="text-xl font-semibold">Roadmap Analytics</h3>
          <span className="rounded-full border border-cyan-500/40 bg-cyan-500/10 px-3 py-1 text-xs uppercase tracking-[0.2em] text-cyan-200">
            100-question path
          </span>
        </div>
        <div className="mt-4 overflow-x-auto">
          <table className="min-w-full text-left text-sm text-slate-200">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400">
                <th className="pb-3 pr-4">User</th>
                <th className="pb-3 pr-4">Completed</th>
                <th className="pb-3 pr-4">Attempts</th>
                <th className="pb-3 pr-4">Progress</th>
                <th className="pb-3">Questions</th>
              </tr>
            </thead>
            <tbody>
              {roadmapProgress.length === 0 ? (
                <tr>
                  <td className="py-4 text-slate-400" colSpan={5}>No roadmap progress yet.</td>
                </tr>
              ) : (
                roadmapProgress.map((user) => (
                  <tr key={user.userId} className="border-b border-slate-900 last:border-0">
                    <td className="py-3 pr-4">
                      <div className="font-medium text-white">{user.name}</div>
                      <div className="text-xs text-slate-400">{user.email}</div>
                    </td>
                    <td className="py-3 pr-4">{user.completedQuestions}</td>
                    <td className="py-3 pr-4">{user.totalAttempts}</td>
                    <td className="py-3 pr-4">{user.completionRate.toFixed(1)}%</td>
                    <td className="py-3">{user.totalQuestions}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      <form className="rounded-3xl border border-slate-800 bg-slate-950/70 p-5" onSubmit={submitRoadmap}>
        <div className="mb-4 flex items-center justify-between gap-3">
          <h3 className="text-xl font-semibold">Push Roadmap to Users</h3>
          <span className="rounded-full border border-violet-500/40 bg-violet-500/10 px-3 py-1 text-xs uppercase tracking-[0.2em] text-violet-200">
            {roadmapTopics.length} topics
          </span>
        </div>
        <p className="mb-3 text-sm text-slate-400">Paste the roadmap JSON and push it to all users. This updates the admin and user roadmap instantly.</p>
        <textarea
          className="field min-h-[260px] font-mono text-xs"
          value={roadmapText}
          onChange={(event) => setRoadmapText(event.target.value)}
          placeholder='[{"topic":"Arrays","description":"...","questions":[{"id":"Q-001","topic":"Arrays","title":"Two Sum","difficulty":"Easy","prompt":"...","tags":["array","practice"]}]}]'
        />
        <div className="mt-4 flex gap-3">
          <button className="btn-primary flex-1" type="submit">
            Push roadmap update
          </button>
          <button className="btn-secondary" type="button" onClick={resetRoadmapToDefault}>
            Reset to default
          </button>
        </div>
      </form>

      <section className="rounded-3xl border border-slate-800 bg-slate-950/70 p-5">
        <h3 className="text-xl font-semibold">User Performance</h3>
        <div className="mt-4 overflow-x-auto">
          <table className="min-w-full text-left text-sm text-slate-200">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400">
                <th className="pb-3 pr-4">User</th>
                <th className="pb-3 pr-4">Runs</th>
                <th className="pb-3 pr-4">Success</th>
                <th className="pb-3 pr-4">Failed</th>
                <th className="pb-3 pr-4">Rate</th>
                <th className="pb-3">Best time</th>
              </tr>
            </thead>
            <tbody>
              {performance.length === 0 ? (
                <tr>
                  <td className="py-4 text-slate-400" colSpan={6}>No execution data yet.</td>
                </tr>
              ) : (
                performance.map((user) => (
                  <tr key={user.userId} className="border-b border-slate-900 last:border-0">
                    <td className="py-3 pr-4">
                      <div className="font-medium text-white">{user.name}</div>
                      <div className="text-xs text-slate-400">{user.email}</div>
                    </td>
                    <td className="py-3 pr-4">{user.totalRuns}</td>
                    <td className="py-3 pr-4">{user.successfulRuns}</td>
                    <td className="py-3 pr-4">{user.failedRuns}</td>
                    <td className="py-3 pr-4">{user.successRate.toFixed(1)}%</td>
                    <td className="py-3">{user.bestExecutionTimeMs} ms</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}