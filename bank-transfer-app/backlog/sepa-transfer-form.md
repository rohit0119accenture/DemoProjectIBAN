# SEPA Transfer Form

The app has a single screen: a SEPA transfer form.

The user can fill in:
- Recipient Name (free text)
- IBAN (auto-formatted into groups of 4 characters, max 34 characters)
- Amount in EUR (with a static "executed on next business day" note)
- Payment Reference (free text, max 140 characters)

On submit, the form POSTs to the backend and shows a success banner. A reset button clears the form.

There is no navigation, no routing, and no other screens. This is the entire frontend.
