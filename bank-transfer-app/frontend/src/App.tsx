import TransferForm from './components/TransferForm';
import './App.css';

export default function App() {
  return (
    <div className="app">
      <header className="app-header">
        <div className="app-header-left">
          <div className="bank-logo">
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="6" width="22" height="3" rx="1.5" fill="white"/>
              <rect x="5" y="11" width="3" height="9" rx="1" fill="white"/>
              <rect x="12.5" y="11" width="3" height="9" rx="1" fill="white"/>
              <rect x="20" y="11" width="3" height="9" rx="1" fill="white"/>
              <rect x="3" y="22" width="22" height="3" rx="1.5" fill="white"/>
              <path d="M14 2L3 6h22L14 2z" fill="white"/>
            </svg>
          </div>
          <div>
            <h1>SEPA Transfer</h1>
            <div className="app-header-subtitle">Demo Bank Online Banking</div>
          </div>
        </div>
        <div className="app-header-badge">Secure Area</div>
      </header>
      <TransferForm />
    </div>
  );
}
