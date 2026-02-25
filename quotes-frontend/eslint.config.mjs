import nextPlugin from 'eslint-config-next';

const eslintConfig = [
  { ignores: ['dist/**', '.next/**'] },
  ...nextPlugin,
];

export default eslintConfig;
