/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      boxShadow: {
        glass: '0 20px 80px rgba(15, 23, 42, 0.35)'
      },
      backgroundImage: {
        'mesh-gradient': 'radial-gradient(circle at top left, rgba(56, 189, 248, 0.25), transparent 28%), radial-gradient(circle at top right, rgba(16, 185, 129, 0.16), transparent 22%), radial-gradient(circle at bottom center, rgba(234, 88, 12, 0.12), transparent 26%)'
      },
      animation: {
        floaty: 'floaty 10s ease-in-out infinite'
      },
      keyframes: {
        floaty: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-10px)' }
        }
      }
    }
  },
  plugins: []
};