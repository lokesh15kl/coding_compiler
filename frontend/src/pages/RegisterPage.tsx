import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../api/axios';
import { fallbackRegister } from '../auth/devAuth';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    try {
      await api.post('/api/auth/register', { name, email, password });
      toast.success('Account created');
      navigate('/login');
    } catch (error: unknown) {
      const hasResponse = typeof error === 'object' && error !== null && 'response' in error;
      if (!hasResponse) {
        try {
          fallbackRegister(name, email, password);
          toast.success('Account created locally');
          navigate('/login');
          return;
        } catch (fallbackError) {
          toast.error(fallbackError instanceof Error ? fallbackError.message : 'Unable to register');
          return;
        }
      }
      toast.error('Unable to register');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="grid min-h-screen place-items-center px-4 py-10">
      <form className="glass-card w-full max-w-md rounded-3xl p-8" onSubmit={submit}>
        <div className="mb-6">
          <p className="text-xs uppercase tracking-[0.28em] text-cyan-300">CodeForge</p>
          <h1 className="mt-2 text-3xl font-semibold">Register</h1>
          <p className="mt-2 text-sm text-slate-400">Create your account to save programs and review history.</p>
        </div>
        <label className="label" htmlFor="name">Name</label>
        <input className="field mb-4" id="name" value={name} onChange={(event) => setName(event.target.value)} placeholder="Your name" />
        <label className="label" htmlFor="email">Email</label>
        <input className="field mb-4" id="email" value={email} onChange={(event) => setEmail(event.target.value)} type="email" placeholder="you@example.com" />
        <label className="label" htmlFor="password">Password</label>
        <input className="field mb-6" id="password" value={password} onChange={(event) => setPassword(event.target.value)} type="password" placeholder="Create a strong password" />
        <button className="btn-primary w-full" disabled={loading} type="submit">
          {loading ? 'Creating account...' : 'Create account'}
        </button>
        <p className="mt-4 text-center text-sm text-slate-400">
          Already have an account? <Link className="text-cyan-300 hover:text-cyan-200" to="/login">Login</Link>
        </p>
      </form>
    </div>
  );
}