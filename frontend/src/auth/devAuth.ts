type DevUser = {
  name: string;
  email: string;
  password: string;
  role: 'USER' | 'ADMIN';
};

const USERS_KEY = 'codeforge-dev-users';

const seedUsers: DevUser[] = [
  { name: 'Admin User', email: 'admin@codeforge.dev', password: 'Admin1234', role: 'ADMIN' },
  { name: 'Demo User', email: 'user@codeforge.dev', password: 'User1234', role: 'USER' }
];

function loadUsers(): DevUser[] {
  const raw = localStorage.getItem(USERS_KEY);
  if (!raw) {
    localStorage.setItem(USERS_KEY, JSON.stringify(seedUsers));
    return seedUsers;
  }

  try {
    const users = JSON.parse(raw) as DevUser[];
    if (Array.isArray(users) && users.length) {
      return users;
    }
  } catch {
    // fall through to reseed
  }

  localStorage.setItem(USERS_KEY, JSON.stringify(seedUsers));
  return seedUsers;
}

function saveUsers(users: DevUser[]) {
  localStorage.setItem(USERS_KEY, JSON.stringify(users));
}

export function fallbackLogin(email: string, password: string) {
  const users = loadUsers();
  const user = users.find((item) => item.email.toLowerCase() === email.toLowerCase() && item.password === password);
  if (!user) {
    throw new Error('Invalid email or password');
  }

  const token = `dev-${btoa(`${user.email}:${user.role}`)}`;
  localStorage.setItem('token', token);
  localStorage.setItem('name', user.name);
  localStorage.setItem('role', user.role);
  localStorage.setItem('auth-mode', 'dev');
  return user;
}

export function fallbackRegister(name: string, email: string, password: string) {
  const users = loadUsers();
  if (users.some((item) => item.email.toLowerCase() === email.toLowerCase())) {
    throw new Error('Email already exists');
  }

  const user: DevUser = { name, email, password, role: 'USER' };
  users.push(user);
  saveUsers(users);
  return user;
}

export function isDevAuthEnabled() {
  return localStorage.getItem('auth-mode') === 'dev';
}
