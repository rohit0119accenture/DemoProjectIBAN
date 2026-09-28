# Backend — Transfer Stub

The backend has a single REST endpoint: POST /api/transfers

It accepts a JSON body with: recipientName, iban, amount, purpose.

It always returns a success response immediately — there is no real processing, no persistence, no database, and no validation. The response is: { "message": "Transfer to [name] submitted successfully.", "status": "SUCCESS" }

There is no user model, no account model, no transaction history, no authentication, and no session management. The app does not differentiate between account types because there are no accounts.
