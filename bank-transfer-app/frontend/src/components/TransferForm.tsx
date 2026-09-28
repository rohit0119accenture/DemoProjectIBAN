import { useState, useRef } from 'react';
import './TransferForm.css';

interface FormData {
  recipientName: string;
  iban: string;
  amount: string;
  purpose: string;
}

const initialFormData: FormData = {
  recipientName: '',
  iban: '',
  amount: '',
  purpose: '',
};

function formatIban(raw: string): string {
  return raw.replace(/[^A-Za-z0-9]/g, '').toUpperCase().replace(/(.{4})/g, '$1 ').trim();
}

function unformatIban(formatted: string): string {
  return formatted.replace(/\s/g, '');
}

function formatAmount(raw: string): string {
  const cleaned = raw.replace(/[^0-9,\.]/g, '');
  return cleaned;
}

export default function TransferForm() {
  const [formData, setFormData] = useState<FormData>(initialFormData);
  const [successMessage, setSuccessMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [focusedField, setFocusedField] = useState<string | null>(null);
  const formRef = useRef<HTMLFormElement>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;

    if (name === 'iban') {
      const raw = unformatIban(value);
      if (raw.length <= 34) {
        setFormData({ ...formData, iban: raw });
      }
      return;
    }

    if (name === 'amount') {
      setFormData({ ...formData, amount: formatAmount(value) });
      return;
    }

    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSuccessMessage('');
    setIsSubmitting(true);

    try {
      const response = await fetch('/api/transfers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });

      const data = await response.json();
      setSuccessMessage(data.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReset = () => {
    setFormData(initialFormData);
    setSuccessMessage('');
  };

  const today = new Date();
  const dateStr = today.toLocaleDateString('en-GB', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });

  return (
    <form className="transfer-form" onSubmit={handleSubmit} ref={formRef}>
      {/* Success Message */}
      {successMessage && (
        <div className="success-banner" role="status">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <circle cx="10" cy="10" r="10" fill="currentColor" opacity="0.15" />
            <path d="M6 10l3 3 5-6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
          <span>{successMessage}</span>
        </div>
      )}

      {/* Card: Empfänger */}
      <section className="form-section">
        <div className="form-section-header">
          <div className="form-section-icon">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <circle cx="9" cy="6" r="3.5" stroke="currentColor" strokeWidth="1.5"/>
              <path d="M2.5 16c0-3.04 2.9-5.5 6.5-5.5s6.5 2.46 6.5 5.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
          </div>
          <h2 className="form-section-title">Recipient</h2>
        </div>

        <div className="form-field">
          <label htmlFor="recipientName" className={`form-label ${focusedField === 'recipientName' ? 'form-label--active' : ''}`}>
            Recipient Name
          </label>
          <input
            id="recipientName"
            type="text"
            name="recipientName"
            value={formData.recipientName}
            onChange={handleChange}
            onFocus={() => setFocusedField('recipientName')}
            onBlur={() => setFocusedField(null)}
            placeholder="John Doe"
            autoComplete="off"
            className="form-input"
          />
        </div>

        <div className="form-field">
          <label htmlFor="iban" className={`form-label ${focusedField === 'iban' ? 'form-label--active' : ''}`}>
            IBAN
          </label>
          <input
            id="iban"
            type="text"
            name="iban"
            value={formatIban(formData.iban)}
            onChange={handleChange}
            onFocus={() => setFocusedField('iban')}
            onBlur={() => setFocusedField(null)}
            placeholder="DE00 0000 0000 0000 0000 00"
            autoComplete="off"
            className="form-input form-input--mono"
            spellCheck={false}
          />
          <div className="form-field-hint">
            {formData.iban.length > 0 && (
              <span className="iban-counter">{formData.iban.length}/22 characters</span>
            )}
          </div>
        </div>
      </section>

      {/* Card: Überweisungsdaten */}
      <section className="form-section" style={{ animationDelay: '80ms' }}>
        <div className="form-section-header">
          <div className="form-section-icon">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <rect x="2" y="3.5" width="14" height="11" rx="2" stroke="currentColor" strokeWidth="1.5"/>
              <path d="M2 7.5h14" stroke="currentColor" strokeWidth="1.5"/>
              <path d="M5.5 11h3" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
          </div>
          <h2 className="form-section-title">Transfer Details</h2>
        </div>

        <div className="form-row">
          <div className="form-field form-field--amount">
            <label htmlFor="amount" className={`form-label ${focusedField === 'amount' ? 'form-label--active' : ''}`}>
              Amount
            </label>
            <div className="form-input-wrapper">
              <input
                id="amount"
                type="text"
                name="amount"
                value={formData.amount}
                onChange={handleChange}
                onFocus={() => setFocusedField('amount')}
                onBlur={() => setFocusedField(null)}
                placeholder="0,00"
                autoComplete="off"
                className="form-input form-input--amount"
                inputMode="decimal"
              />
              <span className="form-input-suffix">EUR</span>
            </div>
          </div>

          <div className="form-field form-field--date">
            <label className="form-label">Execution Date</label>
            <div className="form-static-value">{dateStr}</div>
          </div>
        </div>

        <div className="form-field">
          <label htmlFor="purpose" className={`form-label ${focusedField === 'purpose' ? 'form-label--active' : ''}`}>
            Payment Reference
          </label>
          <textarea
            id="purpose"
            name="purpose"
            value={formData.purpose}
            onChange={handleChange}
            onFocus={() => setFocusedField('purpose')}
            onBlur={() => setFocusedField(null)}
            placeholder="e.g. Invoice No. 12345, Rent January"
            className="form-input form-input--textarea"
            rows={2}
          />
          <div className="form-field-hint">
            <span>{formData.purpose.length}/140 characters</span>
          </div>
        </div>
      </section>

      {/* Actions */}
      <div className="form-actions" style={{ animationDelay: '160ms' }}>
        <button
          type="submit"
          className={`btn btn--primary ${isSubmitting ? 'btn--loading' : ''}`}
          disabled={isSubmitting}
        >
          {isSubmitting ? (
            <>
              <span className="btn-spinner" />
              Processing...
            </>
          ) : (
            <>
              <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                <path d="M15.5 2.5l-7 13-3-5.5-5.5-3 13-7z" fill="currentColor" opacity="0.2"/>
                <path d="M15.5 2.5l-7 13-3-5.5-5.5-3 13-7z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round"/>
              </svg>
              Submit Transfer
            </>
          )}
        </button>
        <button type="button" className="btn btn--ghost" onClick={handleReset}>
          Reset Form
        </button>
      </div>

      {/* Footer Info */}
      <footer className="form-footer">
        <div className="form-footer-item">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <rect x="3" y="6" width="8" height="6" rx="1.5" stroke="currentColor" strokeWidth="1.2"/>
            <path d="M5 6V4.5a2 2 0 014 0V6" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round"/>
          </svg>
          <span>256-bit SSL encrypted</span>
        </div>
        <div className="form-footer-item">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M7 1l1.76 3.57L13 5.18l-3 2.93.71 4.12L7 10.27 3.29 12.23 4 8.11 1 5.18l4.24-.61L7 1z" stroke="currentColor" strokeWidth="1.2" strokeLinejoin="round"/>
          </svg>
          <span>SEPA compliant</span>
        </div>
        <div className="form-footer-item">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <circle cx="7" cy="7" r="5.5" stroke="currentColor" strokeWidth="1.2"/>
            <path d="M7 4v3.5l2.5 1.5" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
          <span>Executed on next business day</span>
        </div>
      </footer>
    </form>
  );
}
