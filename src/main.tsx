import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.tsx';
import { DayflowProvider } from './store/dayflowStore.tsx';
import './index.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <DayflowProvider>
      <App />
    </DayflowProvider>
  </StrictMode>,
);
