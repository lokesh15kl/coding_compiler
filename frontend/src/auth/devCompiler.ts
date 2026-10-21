import { Language } from '../data/languages';

export type DevExecutionRecord = {
  id: string;
  title: string;
  language: Language;
  status: 'SUCCESS' | 'COMPILE_ERROR' | 'RUNTIME_ERROR' | 'TIMEOUT' | 'FAILED';
  executionTime: number;
  memoryUsage: number;
  output: string;
  stderr: string;
  createdAt: string;
};

const HISTORY_KEY = 'codeforge-dev-history';

function loadHistory(): DevExecutionRecord[] {
  try {
    const raw = localStorage.getItem(HISTORY_KEY);
    return raw ? (JSON.parse(raw) as DevExecutionRecord[]) : [];
  } catch {
    return [];
  }
}

function saveHistory(records: DevExecutionRecord[]) {
  localStorage.setItem(HISTORY_KEY, JSON.stringify(records));
}

function unescapeLiteral(value: string) {
  return value
    .replace(/\\n/g, '\n')
    .replace(/\\t/g, '\t')
    .replace(/\\r/g, '\r')
    .replace(/\\"/g, '"')
    .replace(/\\'/g, "'")
    .replace(/\\\\/g, '\\');
}

function extractLiteralValues(code: string, pattern: RegExp) {
  const values: string[] = [];
  for (const match of code.matchAll(pattern)) {
    const value = match[1] ?? match[2] ?? match[0];
    if (value) {
      values.push(unescapeLiteral(value));
    }
  }
  return values;
}

function evaluateCode(language: Language, code: string, input: string) {
  const trimmedInput = input.trim();

  switch (language) {
    case 'java': {
      const values = extractLiteralValues(code, /System\.out\.(?:print|println)\(\s*(?:"([^"\\]*(?:\\.[^"\\]*)*)"|'([^'\\]*(?:\\.[^'\\]*)*)')\s*\)/g);
      if (values.length) {
        return values.join('');
      }
      if (/scanner|nextline|nextint/i.test(code) && trimmedInput) {
        return trimmedInput;
      }
      throw new Error('Unsupported Java snippet in local mode');
    }
    case 'python': {
      const values = extractLiteralValues(code, /print\(\s*(?:"([^"\\]*(?:\\.[^"\\]*)*)"|'([^'\\]*(?:\\.[^'\\]*)*)')\s*\)/g);
      if (values.length) {
        return values.join('\n');
      }
      if (/input\(\)/i.test(code) && trimmedInput) {
        return trimmedInput;
      }
      throw new Error('Unsupported Python snippet in local mode');
    }
    case 'javascript': {
      const values = extractLiteralValues(code, /console\.log\(\s*(?:"([^"\\]*(?:\\.[^"\\]*)*)"|'([^'\\]*(?:\\.[^'\\]*)*)')\s*\)/g);
      if (values.length) {
        return values.join('\n');
      }
      if (/prompt\(|readline/i.test(code) && trimmedInput) {
        return trimmedInput;
      }
      throw new Error('Unsupported JavaScript snippet in local mode');
    }
    case 'c':
    case 'cpp': {
      const values = extractLiteralValues(code, /(?:printf|cout)\s*(?:\([^)]*\)|<<\s*)(?:"([^"\\]*(?:\\.[^"\\]*)*)"|'([^'\\]*(?:\\.[^'\\]*)*)')/g);
      if (values.length) {
        return values.join('');
      }
      if (trimmedInput) {
        return trimmedInput;
      }
      throw new Error('Unsupported C/C++ snippet in local mode');
    }
    default:
      throw new Error('Unsupported language');
  }
}

export function runDevCompiler(language: Language, code: string, input: string, title: string) {
  const start = performance.now();
  try {
    const output = evaluateCode(language, code, input);
    const executionTime = Math.max(1, Math.round(performance.now() - start));
    const record: DevExecutionRecord = {
      id: crypto.randomUUID(),
      title,
      language,
      status: 'SUCCESS',
      executionTime,
      memoryUsage: 0,
      output,
      stderr: '',
      createdAt: new Date().toISOString()
    };
    const history = [record, ...loadHistory()].slice(0, 50);
    saveHistory(history);
    return { output, stderr: '', executionTime, memoryUsage: 0, status: 'SUCCESS' as const };
  } catch (error) {
    const executionTime = Math.max(1, Math.round(performance.now() - start));
    const message = error instanceof Error ? error.message : 'Execution failed';
    const record: DevExecutionRecord = {
      id: crypto.randomUUID(),
      title,
      language,
      status: 'FAILED',
      executionTime,
      memoryUsage: 0,
      output: '',
      stderr: message,
      createdAt: new Date().toISOString()
    };
    const history = [record, ...loadHistory()].slice(0, 50);
    saveHistory(history);
    return { output: '', stderr: message, executionTime, memoryUsage: 0, status: 'FAILED' as const };
  }
}

export function loadDevHistory() {
  return loadHistory();
}
