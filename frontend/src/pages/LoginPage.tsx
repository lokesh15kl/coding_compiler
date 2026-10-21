import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../api/axios';
import { fallbackLogin } from '../auth/devAuth';

type LoginResponse = {
  token: string;
  name: string;
  role: string;
};

export default function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      const { data } = await api.post<LoginResponse>('/api/auth/login', { email, password });
      localStorage.setItem('token', data.token);
      localStorage.setItem('name', data.name);
      localStorage.setItem('role', data.role);
      toast.success('Login successful');
      navigate('/');
    } catch (error: unknown) {
      const hasResponse = typeof error === 'object' && error !== null && 'response' in error;
      if (!hasResponse) {
        try {
          fallbackLogin(email, password);
          toast.success('Logged in with local dev auth');
          navigate('/');
          return;
        } catch {
          toast.error('Invalid email or password');
          return;
        }
      }
      toast.error('Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="grid min-h-screen place-items-center px-4 py-10">
      <form className="glass-card w-full max-w-md rounded-3xl p-8" onSubmit={submit}>
        <div className="mb-6">
          <p className="text-xs uppercase tracking-[0.28em] text-cyan-300">CodeForge</p>
          <h1 className="mt-2 text-3xl font-semibold">Login</h1>
          <p className="mt-2 text-sm text-slate-400">Access your compiler workspace and saved programs.</p>
        </div>
        <label className="label" htmlFor="email">Email</label>
        <input className="field mb-4" id="email" value={email} onChange={(event) => setEmail(event.target.value)} type="email" placeholder="you@example.com" />
        <label className="label" htmlFor="password">Password</label>
        <input className="field mb-6" id="password" value={password} onChange={(event) => setPassword(event.target.value)} type="password" placeholder="••••••••" />
        <button className="btn-primary w-full" disabled={loading} type="submit">
          {loading ? 'Signing in...' : 'Login'}
        </button>
        <p className="mt-4 text-center text-sm text-slate-400">
          No account? <Link className="text-cyan-300 hover:text-cyan-200" to="/register">Register</Link>
        </p>
      </form>
    </div>
  );
}