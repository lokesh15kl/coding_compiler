import { NavLink, Outlet, useNavigate } from 'react-router-dom';

const baseNavItems = [
  { to: '/', label: 'Dashboard' },
  { to: '/compiler', label: 'Compiler' },
  { to: '/history', label: 'History' },
  { to: '/programs', label: 'Programs' },
  { to: '/practice', label: 'Practice' },
  { to: '/profile', label: 'Profile' }
];

export default function Layout() {
  const navigate = useNavigate();
  const userName = localStorage.getItem('name') ?? 'Coder';
  const userRole = localStorage.getItem('role') ?? 'USER';
  const navItems = userRole === 'ADMIN' ? [...baseNavItems, { to: '/admin', label: 'Admin' }] : baseNavItems;

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('name');
    localStorage.removeItem('role');
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-white to-blue-50 text-slate-900">
      <div className="mx-auto flex min-h-screen max-w-7xl gap-5 p-4 lg:p-6">
        {/* Sidebar */}
        <aside className="hidden w-64 rounded-2xl bg-white border border-slate-200 shadow-sm p-4 lg:flex lg:flex-col">
          <div className="mb-8 rounded-xl bg-gradient-to-br from-blue-50 to-blue-100 border border-blue-200 p-4">
            <div className="text-xs uppercase tracking-[0.3em] text-blue-600 font-bold">CodeForge</div>
            <div className="mt-2 text-xl font-bold text-slate-900">Online Compiler</div>
            <p className="mt-2 text-sm text-slate-700">Build, run, save, and share code from one workspace.</p>
          </div>

          <nav className="space-y-2">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) =>
                  `block rounded-lg px-4 py-3 text-sm font-medium transition-all ${
                    isActive
                      ? 'bg-blue-600 text-white shadow-sm'
                      : 'text-slate-700 hover:bg-slate-100'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          <button
            className="mt-auto rounded-lg bg-slate-900 text-white px-4 py-3 text-sm font-medium hover:bg-slate-800 transition-colors"
            onClick={logout}
            type="button"
          >
            Logout
          </button>
        </aside>

        <div className="flex min-w-0 flex-1 flex-col gap-5">
          {/* Header */}
          <header className="rounded-2xl bg-white border border-slate-200 shadow-sm px-6 py-4">
            <div className="flex items-center justify-between gap-4">
              <div>
                <p className="text-xs uppercase tracking-[0.28em] text-blue-600 font-bold">Welcome back</p>
                <h1 className="mt-1 text-2xl font-bold text-slate-900">{userName}</h1>
              </div>
              <div className="flex items-center gap-3">
                <div className="hidden rounded-lg border border-slate-300 bg-slate-100 px-4 py-2 text-sm text-slate-700 font-medium md:block">
                  {userRole === 'ADMIN' ? '👨‍💼 Admin Panel' : '💡 Light Mode'}
                </div>
                <button className="rounded-lg bg-slate-900 text-white px-4 py-2 text-sm font-medium hover:bg-slate-800 transition-colors lg:hidden" onClick={logout} type="button">
                  Logout
                </button>
              </div>
            </div>
          </header>

          {/* Main Content */}
          <main className="rounded-2xl bg-white border border-slate-200 shadow-sm min-h-0 flex-1 p-6">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  );
}