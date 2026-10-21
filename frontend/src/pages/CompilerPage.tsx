import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../api/axios';
import { runDevCompiler } from '../auth/devCompiler';
import CodeEditor from '../components/CodeEditor';
import { languageOptions, Language } from '../data/languages';

const languageToMonaco: Record<Language, string> = {
  java: 'java',
  python: 'python',
  c: 'c',
  cpp: 'cpp',
  javascript: 'javascript'
};

const themeOptions = [
  { value: 'light', label: 'Light', monaco: 'light' as const, pageClass: 'theme-light' },
  { value: 'dark', label: 'Dark', monaco: 'vs-dark' as const, pageClass: 'theme-midnight' }
];

const requiresInput = /(Scanner|nextInt\(|nextLine\(|input\(|scanf\(|cin\s*>>|prompt\()/i;

export default function CompilerPage() {
  const navigate = useNavigate();
  const [language, setLanguage] = useState<Language>('java');
  const [code, setCode] = useState(languageOptions[0].starter);
  const [input, setInput] = useState('');
  const [programName, setProgramName] = useState('My Program');
  const [output, setOutput] = useState('');
  const [stderr, setStderr] = useState('');
  const [executionTime, setExecutionTime] = useState<number | undefined>();
  const [memoryUsage, setMemoryUsage] = useState<number | undefined>();
  const [shareUrl, setShareUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [theme, setTheme] = useState<string>('white');
  const [currentRoadmapQuestionId, setCurrentRoadmapQuestionId] = useState<string | undefined>();
  const [markingAttempt, setMarkingAttempt] = useState(false);
  
  // Problem statement details
  const [problemStatement, setProblemStatement] = useState('');
  const [inputFormat, setInputFormat] = useState('');
  const [outputFormat, setOutputFormat] = useState('');
  const [sampleInput, setSampleInput] = useState('');
  const [sampleOutput, setSampleOutput] = useState('');
  const [hasProblem, setHasProblem] = useState(false);

  const monacoLanguage = useMemo(() => languageToMonaco[language], [language]);
  const selectedTheme = useMemo(
    () => themeOptions.find((option) => option.value === theme) ?? themeOptions[0],
    [theme]
  );

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  useEffect(() => {
    const savedPractice = localStorage.getItem('practiceProblem');
    if (!savedPractice) return;
    try {
      const challenge = JSON.parse(savedPractice) as {
        title?: string;
        language?: string;
        statement?: string;
        inputFormat?: string;
        outputFormat?: string;
        sampleInput?: string;
        sampleOutput?: string;
        isRoadmapQuestion?: boolean;
        questionId?: string;
      };

      if (challenge.isRoadmapQuestion && challenge.questionId) {
        setCurrentRoadmapQuestionId(challenge.questionId);
      }

      if (challenge.title) {
        setProgramName(challenge.title);
      }
      
      // Store problem details for display
      if (challenge.statement) {
        setProblemStatement(challenge.statement);
        setHasProblem(true);
      }
      if (challenge.inputFormat) {
        setInputFormat(challenge.inputFormat);
      }
      if (challenge.outputFormat) {
        setOutputFormat(challenge.outputFormat);
      }
      if (challenge.sampleInput) {
        setSampleInput(challenge.sampleInput);
        setInput(challenge.sampleInput);
      }
      if (challenge.sampleOutput) {
        setSampleOutput(challenge.sampleOutput);
      }
      
      if (challenge.language) {
        const nextLanguage = challenge.language.toLowerCase();
        if (nextLanguage === 'c++' || nextLanguage === 'cpp') {
          setLanguage('cpp');
        } else if (nextLanguage === 'javascript' || nextLanguage === 'js') {
          setLanguage('javascript');
        } else if (nextLanguage === 'python') {
          setLanguage('python');
        } else if (nextLanguage === 'c') {
          setLanguage('c');
        } else {
          setLanguage('java');
        }
      }
      
      // Set starter code
      const starter = challenge.statement
        ? (challenge.language?.toLowerCase() === 'python'
            ? "# Write your solution here\n"
            : challenge.language?.toLowerCase() === 'javascript'
              ? "// Write your solution here\n"
              : "public class Main {\n    public static void main(String[] args) {\n        // Write your solution here\n    }\n}\n")
        : code;
      setCode(starter);
      
      localStorage.removeItem('practiceProblem');
    } catch {
      localStorage.removeItem('practiceProblem');
    }
  }, []);

  const onLanguageChange = (nextLanguage: Language) => {
    setLanguage(nextLanguage);
    setCode(languageOptions.find((item) => item.value === nextLanguage)?.starter ?? '');
  };

  const validateInput = () => {
    const needsInput = requiresInput.test(code) && input.trim().length === 0;
    if (needsInput) {
      setStderr('Please provide input before running this program.');
      setOutput('');
      toast.error('Please provide input before running this program.');
      return false;
    }
    setStderr('');
    return true;
  };

  const runCode = async () => {
    if (!validateInput()) {
      return;
    }

    setLoading(true);
    setShareUrl('');
    try {
      const { data } = await api.post('/api/compiler/run', { language, code, input });
      setOutput(data.output ?? '');
      setStderr(data.stderr ?? '');
      setExecutionTime(data.executionTime);
      setMemoryUsage(data.memoryUsage);
      toast.success('Execution completed');
    } catch (error: unknown) {
      const hasAxiosResponse = typeof error === 'object' && error !== null && 'response' in error;
      if (!hasAxiosResponse) {
        const result = runDevCompiler(language, code, input, programName);
        setOutput(result.output);
        setStderr(result.stderr);
        setExecutionTime(result.executionTime);
        setMemoryUsage(result.memoryUsage);
        toast.success('Execution completed in local dev mode');
        return;
      }
      let message = 'Execution failed';
      const maybeMessage =
        typeof error === 'object' && error !== null && 'response' in error &&
        typeof (error as { response?: { data?: { message?: string } } }).response?.data?.message === 'string'
          ? (error as { response?: { data?: { message?: string } } }).response?.data?.message
          : undefined;
      if (typeof maybeMessage === 'string') {
        message = maybeMessage;
      }
      setStderr(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  const saveProgram = async () => {
    try {
      const { data } = await api.post('/api/program/save', { title: programName, language, code });
      setShareUrl(`${window.location.origin}/share/${data.shareId}`);
      toast.success('Program saved');
    } catch {
      toast.error('Unable to save program');
    }
  };

  const downloadProgram = () => {
    const extension = languageOptions.find((item) => item.value === language)?.extension ?? '.txt';
    const blob = new Blob([code], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = `${programName || 'program'}${extension}`;
    anchor.click();
    URL.revokeObjectURL(url);
  };

  const markRoadmapQuestionComplete = async () => {
    if (!currentRoadmapQuestionId) return;
    setMarkingAttempt(true);
    try {
      await api.post(`/api/roadmap/questions/${currentRoadmapQuestionId}/attempt?completed=true`);
      toast.success('Roadmap question marked as complete!');
      setCurrentRoadmapQuestionId(undefined);
    } catch {
      toast.error('Unable to mark question as complete');
    } finally {
      setMarkingAttempt(false);
    }
  };

  return (
    <div className={`min-h-screen flex flex-col ${selectedTheme.pageClass}`}>
      {/* HEADER */}
      <div className={`border-b ${theme === 'light' ? 'border-gray-200 bg-white' : 'border-gray-700 bg-gray-900'} px-6 py-3`}>
        <div className="flex items-center justify-between max-w-[1600px] mx-auto">
          <div className="flex items-center gap-4">
            <button onClick={() => navigate('/practice')} className={`text-sm font-medium ${theme === 'light' ? 'text-blue-600 hover:text-blue-700' : 'text-blue-400 hover:text-blue-300'}`}>← Back to Practice</button>
            <h1 className={`text-xl font-bold ${theme === 'light' ? 'text-gray-900' : 'text-white'}`}>{programName}</h1>
          </div>
          <div className="flex items-center gap-3">
            <select className={`text-sm px-3 py-2 rounded border font-medium ${theme === 'light' ? 'bg-white border-gray-300 text-gray-900' : 'bg-gray-800 border-gray-600 text-white'}`} value={language} onChange={(event) => onLanguageChange(event.target.value as Language)}>
              {languageOptions.map((option) => (
                <option key={option.value} value={option.value}>{option.label}</option>
              ))}
            </select>
            <select className={`text-sm px-3 py-2 rounded border font-medium ${theme === 'light' ? 'bg-white border-gray-300 text-gray-900' : 'bg-gray-800 border-gray-600 text-white'}`} value={theme} onChange={(event) => setTheme(event.target.value)}>
              {themeOptions.map((option) => (
                <option key={option.value} value={option.value}>{option.label}</option>
              ))}
            </select>
            <button className={`text-sm font-medium px-4 py-2 rounded transition-colors ${theme === 'light' ? 'bg-blue-600 text-white hover:bg-blue-700' : 'bg-blue-600 text-white hover:bg-blue-700'}`} onClick={runCode} disabled={loading}>{loading ? 'Running...' : '▶ Run'}</button>
            <button className={`text-sm font-medium px-4 py-2 rounded border transition-colors ${theme === 'light' ? 'border-gray-300 text-gray-700 hover:bg-gray-100' : 'border-gray-600 text-gray-300 hover:bg-gray-800'}`} onClick={saveProgram}>💾 Save</button>
            <button className={`text-sm font-medium px-4 py-2 rounded border transition-colors ${theme === 'light' ? 'border-gray-300 text-gray-700 hover:bg-gray-100' : 'border-gray-600 text-gray-300 hover:bg-gray-800'}`} onClick={downloadProgram}>⬇ Download</button>
            {currentRoadmapQuestionId && (
              <button className={`text-sm font-medium px-4 py-2 rounded transition-colors ${theme === 'light' ? 'bg-green-600 text-white hover:bg-green-700' : 'bg-green-600 text-white hover:bg-green-700'}`} onClick={markRoadmapQuestionComplete} disabled={markingAttempt}>{markingAttempt ? '...' : '✓ Done'}</button>
            )}
          </div>
        </div>
      </div>

      {/* MAIN CONTENT - 3 PANEL LAYOUT */}
      <div className="flex-1 flex gap-0 overflow-hidden">
        {/* LEFT PANEL: Problem Statement */}
        {hasProblem && (
          <div className={`w-1/3 overflow-y-auto border-r ${theme === 'light' ? 'border-gray-200 bg-gradient-to-br from-white to-gray-50' : 'border-gray-700 bg-gray-900'} p-6 space-y-6`}>
            {/* Problem Title */}
            {problemStatement && (
              <div>
                <h2 className={`text-2xl font-bold mb-4 ${theme === 'light' ? 'text-gray-900' : 'text-white'}`}>{programName}</h2>
                <p className={`${theme === 'light' ? 'text-gray-700' : 'text-gray-300'} whitespace-pre-wrap text-sm leading-relaxed`}>{problemStatement}</p>
              </div>
            )}

            {/* Input Format */}
            {inputFormat && (
              <div>
                <h3 className={`font-semibold mb-3 text-sm uppercase tracking-wide ${theme === 'light' ? 'text-gray-900' : 'text-gray-100'}`}>📥 Input Format</h3>
                <div className={`text-xs p-4 rounded-lg border whitespace-pre-wrap ${theme === 'light' ? 'bg-blue-50 border-blue-200 text-gray-800' : 'bg-gray-800 border-gray-700 text-gray-300'}`}>
                  {inputFormat}
                </div>
              </div>
            )}

            {/* Output Format */}
            {outputFormat && (
              <div>
                <h3 className={`font-semibold mb-3 text-sm uppercase tracking-wide ${theme === 'light' ? 'text-gray-900' : 'text-gray-100'}`}>📤 Output Format</h3>
                <div className={`text-xs p-4 rounded-lg border whitespace-pre-wrap ${theme === 'light' ? 'bg-green-50 border-green-200 text-gray-800' : 'bg-gray-800 border-gray-700 text-gray-300'}`}>
                  {outputFormat}
                </div>
              </div>
            )}

            {/* Sample Input */}
            {sampleInput && (
              <div>
                <h3 className={`font-semibold mb-3 text-sm uppercase tracking-wide ${theme === 'light' ? 'text-gray-900' : 'text-gray-100'}`}>📋 Example Input</h3>
                <pre className={`text-xs p-4 rounded-lg border font-mono overflow-x-auto ${theme === 'light' ? 'bg-gray-100 border-gray-300 text-gray-900' : 'bg-gray-800 border-gray-700 text-gray-200'}`}>
                  {sampleInput}
                </pre>
              </div>
            )}

            {/* Sample Output */}
            {sampleOutput && (
              <div>
                <h3 className={`font-semibold mb-3 text-sm uppercase tracking-wide ${theme === 'light' ? 'text-gray-900' : 'text-gray-100'}`}>✅ Expected Output</h3>
                <pre className={`text-xs p-4 rounded-lg border font-mono overflow-x-auto ${theme === 'light' ? 'bg-gray-100 border-gray-300 text-gray-900' : 'bg-gray-800 border-gray-700 text-gray-200'}`}>
                  {sampleOutput}
                </pre>
              </div>
            )}
          </div>
        )}

        {/* CENTER PANEL: Code Editor */}
        <div className={`${hasProblem ? 'w-1/3' : 'w-1/2'} flex flex-col overflow-hidden border-r ${theme === 'light' ? 'border-gray-200' : 'border-gray-700'}`}>
          <div className="flex-1 overflow-hidden">
            <CodeEditor
              language={monacoLanguage}
              code={code}
              onChange={setCode}
              theme={selectedTheme.monaco}
            />
          </div>
        </div>

        {/* RIGHT PANEL: Input & Output */}
        <div className={`${hasProblem ? 'w-1/3' : 'w-1/2'} flex flex-col overflow-hidden`}>
          {/* Tabs */}
          <div className={`flex border-b ${theme === 'light' ? 'bg-white border-gray-200' : 'bg-gray-800 border-gray-700'}`}>
            <button className={`flex-1 px-4 py-3 text-sm font-medium text-center border-b-2 ${theme === 'light' ? 'border-blue-600 text-blue-600' : 'border-blue-500 text-blue-400'}`}>
              📥 Input
            </button>
            <button className={`flex-1 px-4 py-3 text-sm font-medium text-center border-b-2 border-transparent ${theme === 'light' ? 'text-gray-600 hover:text-gray-900' : 'text-gray-400 hover:text-gray-200'}`}>
              📤 Output
            </button>
          </div>

          {/* Content */}
          <div className={`flex-1 flex gap-4 overflow-hidden p-4 ${theme === 'light' ? 'bg-gray-50' : 'bg-gray-900'}`}>
            {/* Input Area */}
            <div className="flex-1 flex flex-col">
              <label className={`text-xs font-semibold mb-2 ${theme === 'light' ? 'text-gray-700' : 'text-gray-300'}`}>
                Input
              </label>
              <textarea
                className={`flex-1 text-xs font-mono p-3 rounded-lg border resize-none focus:outline-none focus:ring-2 ${theme === 'light' ? 'bg-white border-gray-300 text-gray-900 focus:ring-blue-500' : 'bg-gray-800 border-gray-600 text-gray-100 focus:ring-blue-500'}`}
                value={input}
                onChange={(event) => setInput(event.target.value)}
                placeholder="Paste your input here..."
                spellCheck="false"
              />
            </div>

            {/* Output Area */}
            <div className="flex-1 flex flex-col">
              <label className={`text-xs font-semibold mb-2 ${theme === 'light' ? 'text-gray-700' : 'text-gray-300'}`}>
                Output
              </label>
              <div className={`flex-1 text-xs font-mono p-3 rounded-lg border overflow-y-auto ${theme === 'light' ? 'bg-white border-gray-300 text-gray-900' : 'bg-gray-800 border-gray-600 text-gray-100'}`}>
                {output ? (
                  <pre className="whitespace-pre-wrap break-words">{output}</pre>
                ) : stderr ? (
                  <pre style={{ color: 'rgb(239, 68, 68)' }} className="whitespace-pre-wrap break-words">
                    {stderr}
                  </pre>
                ) : (
                  <span className={theme === 'light' ? 'text-gray-400' : 'text-gray-500'}>No output yet</span>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}