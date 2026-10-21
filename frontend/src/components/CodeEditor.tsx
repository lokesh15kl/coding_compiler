import Editor from '@monaco-editor/react';

type Props = {
  language: string;
  code: string;
  onChange: (value: string) => void;
  theme?: 'vs-dark' | 'light';
};

export default function CodeEditor({ language, code, onChange, theme = 'vs-dark' }: Props) {
  return (
    <div className="editor-surface overflow-hidden rounded-3xl border shadow-glass">
      <Editor
        height="520px"
        theme={theme}
        language={language}
        value={code}
        onChange={(value) => onChange(value ?? '')}
        options={{
          fontSize: 15,
          minimap: { enabled: false },
          smoothScrolling: true,
          automaticLayout: true,
          scrollBeyondLastLine: false,
          wordWrap: 'on',
          tabSize: 2,
          fontLigatures: true,
          lineNumbersMinChars: 3,
          padding: { top: 18, bottom: 16 }
        }}
      />
    </div>
  );
}