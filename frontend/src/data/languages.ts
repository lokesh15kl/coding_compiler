export type Language = 'java' | 'python' | 'c' | 'cpp' | 'javascript';

export const languageOptions = [
  { label: 'Java', value: 'java' as const, extension: '.java', starter: `public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello, Java");\n    }\n}` },
  { label: 'Python', value: 'python' as const, extension: '.py', starter: `print("Hello, Python")` },
  { label: 'C', value: 'c' as const, extension: '.c', starter: `#include <stdio.h>\n\nint main() {\n    printf("Hello, C\\n");\n    return 0;\n}` },
  { label: 'C++', value: 'cpp' as const, extension: '.cpp', starter: `#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n    cout << "Hello, C++" << endl;\n    return 0;\n}` },
  { label: 'JavaScript', value: 'javascript' as const, extension: '.js', starter: `console.log('Hello, JavaScript');` }
];