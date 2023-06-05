/** @type {import('tailwindcss').Config} */
const colors = require('tailwindcss/colors')

// color classes to be safelisted for dynamic classes, this is because tailwind
// doesn't know about dynamic classes and will purge them by default
const colorClasses = [
  'red-900',
  'blue-900',
  'gray-900',
];

module.exports = {
  content: ["./pages/**/*.{tsx,jsx}", "./components/**/*.{tsx,jsx}"],
  safelist: [
    ...colorClasses.map(color => `bg-${color}`),
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Source Sans Pro", "Arial", "sans-serif"],
      },
      gridTemplateRows: {
        '[auto,auto,1fr]': 'auto auto 1fr',
      },
      colors: {
        ...colors,
      }
    },
  },
  plugins: [
    require('@tailwindcss/aspect-ratio'),
  ],
};
