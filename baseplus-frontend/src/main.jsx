import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { App } from './core/App.jsx';
import { AuthProvider } from './core/auth/AuthProvider.jsx';
import { ThemeProvider } from './core/theme/ThemeProvider.jsx';
import { BrandingProvider } from './shared/branding/BrandingProvider.jsx';
import './core/styles/global.css';
import './shared/components/components.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <ThemeProvider>
        <BrandingProvider>
          <AuthProvider>
            <App />
          </AuthProvider>
        </BrandingProvider>
      </ThemeProvider>
    </BrowserRouter>
  </React.StrictMode>,
);
