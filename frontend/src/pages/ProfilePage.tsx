import { FormEvent, useState } from 'react';
import { toast } from 'react-toastify';
import api from '../api/axios';

export default function ProfilePage() {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const changePassword = async (event: FormEvent) => {
    event.preventDefault();
    try {
      await api.post('/api/auth/change-password', { currentPassword, newPassword });
      toast.success('Password updated');
      setCurrentPassword('');
      setNewPassword('');
    } catch {
      toast.error('Unable to update password');
    }
  };

  const deleteAccount = async () => {
    try {
      await api.delete('/api/auth/me');
      localStorage.clear();
      window.location.href = '/login';
    } catch {
      toast.error('Unable to delete account');
    }
  };

  return (
    <div className="grid gap-6 xl:grid-cols-2">
      <form className="rounded-3xl border border-slate-800 bg-slate-950/70 p-5" onSubmit={changePassword}>
        <h2 className="text-xl font-semibold">Change password</h2>
        <div className="mt-4 space-y-4">
          <input className="field" type="password" placeholder="Current password" value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} />
          <input className="field" type="password" placeholder="New password" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} />
          <button className="btn-primary" type="submit">Update password</button>
        </div>
      </form>

      <div className="rounded-3xl border border-rose-900/40 bg-rose-950/30 p-5">
        <h2 className="text-xl font-semibold text-rose-100">Delete account</h2>
        <p className="mt-2 text-sm text-rose-200/80">This permanently removes your user profile and owned programs.</p>
        <button className="btn-secondary mt-4 border-rose-900/60 bg-rose-950/40 text-rose-100 hover:border-rose-700" type="button" onClick={deleteAccount}>
          Delete account
        </button>
      </div>
    </div>
  );
}