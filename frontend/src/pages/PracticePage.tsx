import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../api/axios';

type Problem = {
  id: number;
  title: string;
  difficulty: string;
  language: string;
  statement: string;
  inputFormat: string;
  outputFormat: string;
  sampleInput: string;
  sampleOutput: string;
};

type Topic = {
  id: number;
  name: string;
  description: string;
  problems: Problem[];
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

type RoadmapProgress = {
  questionId: string;
  topic: string;
  title: string;
  attempts: number;
  completed: boolean;
  updatedAt: string;
};

export default function PracticePage() {
  const navigate = useNavigate();
  const [topics, setTopics] = useState<Topic[]>([]);
  const [roadmap, setRoadmap] = useState<RoadmapTopic[]>([]);
  const [progress, setProgress] = useState<RoadmapProgress[]>([]);
  const [loading, setLoading] = useState(true);
  const [expandedTopics, setExpandedTopics] = useState<Set<string>>(new Set());

  useEffect(() => {
    Promise.all([
      api.get('/api/practice'),
      api.get('/api/roadmap'),
      api.get('/api/roadmap/progress')
    ])
      .then(([practiceResponse, roadmapResponse, progressResponse]) => {
        setTopics(practiceResponse.data);
        setRoadmap(roadmapResponse.data);
        setProgress(progressResponse.data);
      })
      .catch(() => {
        toast.error('Unable to load practice roadmap');
      })
      .finally(() => setLoading(false));
  }, []);

  const openChallenge = (problem: Problem) => {
    localStorage.setItem('practiceProblem', JSON.stringify(problem));
    navigate('/compiler');
  };

  const attemptRoadmapQuestion = (question: RoadmapQuestion) => {
    // Generate realistic sample input/output based on question type
    const getSampleData = (title: string) => {
      const samples: Record<string, { input: string; output: string; format: string }> = {
        'Two Sum': { input: '5\n2 7 11 15\n9', output: '0 1', format: 'First line: n (array size)\nSecond line: n space-separated integers\nThird line: target sum\n\nOutput the indices of two numbers that add up to target' },
        'Best Time to Buy and Sell Stock': { input: '7\n7 1 5 3 6 4 0', output: '5', format: 'First line: n (array size)\nSecond line: n space-separated prices\n\nOutput maximum profit' },
        'Product of Array Except Self': { input: '4\n1 2 3 4', output: '24 12 8 6', format: 'First line: n\nSecond line: n integers\n\nOutput space-separated products' },
        'Maximum Subarray': { input: '5\n-2 1 -3 4 -1', output: '6', format: 'First line: n\nSecond line: n integers\n\nOutput maximum sum of subarray' },
        'Reverse Linked List': { input: '5\n1 2 3 4 5', output: '5 4 3 2 1', format: 'First line: n (size)\nSecond line: n integers (list values)\n\nOutput reversed list' },
        'Valid Parentheses': { input: '{[]}', output: 'true', format: 'Input: string with parentheses\nOutput: true or false if valid' },
        'Number of Islands': { input: '3 3\n1 1 0\n1 0 1\n0 1 1', output: '3', format: 'First line: m n (dimensions)\nNext m lines: grid values\n\nOutput count of islands' },
        'Climbing Stairs': { input: '5', output: '8', format: 'Input: n (steps)\nOutput: number of ways' },
      };
      return samples[title] || { input: 'Enter your input here', output: 'Expected output', format: 'Problem format' };
    };

    const sample = getSampleData(question.title);
    const roadmapProblem = {
      id: question.id,
      title: question.title,
      difficulty: question.difficulty,
      language: 'Java',
      statement: question.prompt,
      inputFormat: sample.format,
      outputFormat: 'Output the result as specified',
      sampleInput: sample.input,
      sampleOutput: sample.output,
      isRoadmapQuestion: true,
      questionId: question.id
    };
    localStorage.setItem('practiceProblem', JSON.stringify(roadmapProblem));
    navigate('/compiler');
  };

  const toggleTopic = (topicName: string) => {
    setExpandedTopics((current) => {
      const next = new Set(current);
      if (next.has(topicName)) {
        next.delete(topicName);
      } else {
        next.add(topicName);
      }
      return next;
    });
  };

  const markRoadmapQuestion = async (question: RoadmapQuestion, completed: boolean) => {
    try {
      const { data } = await api.post(`/api/roadmap/questions/${question.id}/attempt?completed=${completed}`);
      setProgress((current) => {
        const filtered = current.filter((item) => item.questionId !== data.questionId);
        return [data, ...filtered];
      });
      toast.success(`${question.title} ${completed ? 'marked complete' : 'attempt logged'}`);
    } catch {
      toast.error('Unable to update roadmap progress');
    }
  };

  const getProgressState = (questionId: string) => progress.find((item) => item.questionId === questionId);

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50">
      <div className="mx-auto max-w-6xl px-6 py-8">
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-slate-900">Practice Roadmap</h1>
          <p className="mt-2 text-lg text-slate-600">100 curated problems to master coding</p>
        </div>

        {loading ? (
          <div className="rounded-xl border border-slate-200 bg-white p-8 text-center text-slate-600">Loading roadmap…</div>
        ) : null}

        {!loading && roadmap.length === 0 ? (
          <div className="rounded-xl border border-slate-200 bg-white p-8 text-center text-slate-600">
            No roadmap available yet.
          </div>
        ) : null}

        <div className="space-y-4">
          {roadmap.map((topic, index) => {
            const isExpanded = expandedTopics.has(topic.topic);
            const completedCount = topic.questions.filter(
              (q) => getProgressState(q.id)?.completed
            ).length;

            return (
              <div
                key={topic.topic}
                className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm hover:shadow-md transition-shadow"
              >
                {/* Topic Header - Expandable */}
                <button
                  className="w-full px-6 py-5 text-left hover:bg-slate-50 transition-colors flex items-center justify-between"
                  onClick={() => toggleTopic(topic.topic)}
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-4">
                      <span className={`text-2xl transition-transform ${isExpanded ? 'rotate-90' : ''}`}>
                        ▶
                      </span>
                      <div>
                        <h3 className="text-xl font-bold text-slate-900">{topic.topic}</h3>
                        <p className="mt-1 text-sm text-slate-600">{topic.description}</p>
                      </div>
                    </div>
                  </div>
                  <div className="ml-4 text-right">
                    <div className="text-sm font-semibold text-slate-900">
                      {completedCount}/{topic.questions.length}
                    </div>
                    <div className="text-xs text-slate-500">Completed</div>
                  </div>
                </button>

                {/* Expanded Content */}
                {isExpanded && (
                  <div className="border-t border-slate-200 bg-slate-50">
                    <div className="grid gap-4 p-6 md:grid-cols-2 lg:grid-cols-3">
                      {topic.questions.map((question) => {
                        const progressState = getProgressState(question.id);
                        const difficultyColor = {
                          Easy: 'bg-green-100 text-green-800 border-green-200',
                          Medium: 'bg-yellow-100 text-yellow-800 border-yellow-200',
                          Hard: 'bg-red-100 text-red-800 border-red-200'
                        }[question.difficulty] || 'bg-gray-100 text-gray-800 border-gray-200';

                        return (
                          <div
                            key={question.id}
                            className="rounded-lg border border-slate-200 bg-white p-4 hover:shadow-lg transition-shadow"
                          >
                            <div className="mb-3 flex items-start justify-between gap-2">
                              <div className="flex-1">
                                <h4 className="font-semibold text-slate-900">{question.title}</h4>
                              </div>
                              {progressState?.completed && (
                                <span className="flex-shrink-0 rounded-full bg-green-500 px-2 py-1 text-xs font-semibold text-white">
                                  ✓
                                </span>
                              )}
                            </div>

                            <div className="mb-4 flex flex-wrap gap-2">
                              <span className={`rounded-full border px-3 py-1 text-xs font-semibold ${difficultyColor}`}>
                                {question.difficulty}
                              </span>
                              {progressState && (
                                <span className="rounded-full bg-blue-100 px-3 py-1 text-xs font-semibold text-blue-800 border border-blue-200">
                                  {progressState.attempts} attempt{progressState.attempts !== 1 ? 's' : ''}
                                </span>
                              )}
                            </div>

                            <p className="mb-4 line-clamp-2 text-sm text-slate-600">{question.prompt}</p>

                            <button
                              className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 transition-colors"
                              type="button"
                              onClick={() => attemptRoadmapQuestion(question)}
                            >
                              Solve Now
                            </button>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
