import { api } from './api.js';
  import { formatDate, formatTime, formatCurrency, escapeHtml } from './utils.js';
  import { navigateTo } from './app.js';
  
  export function renderCheckout(container, data) {
      container.innerHTML = `
          <div class="steps">
              <div class="step done"><span class="step-number">✓</span> Usluga</div>
              <div class="step done"><span class="step-number">✓</span> Termin</div>
              <div class="step active"><span class="step-number">3</span> Rezervacija</div>
          </div>
  
          <div class="card">
              <h2>Vaši podaci</h2>
              <p class="text-muted mb-2">Usluga: <strong>${escapeHtml(data.serviceName)}</strong> — ${formatCurrency(data.servicePrice)}</p>
  
              <div class="form-group">
                  <label for="guest-name">Ime i prezime</label>
                  <input type="text" id="guest-name" placeholder="Ana Horvat" />
              </div>
              <div class="form-group">
                  <label for="guest-email">Email</label>
                  <input type="email" id="guest-email" placeholder="ana@primjer.hr" />
              </div>
              <div class="form-group">
                  <label for="guest-phone">Telefon</label>
                  <input type="tel" id="guest-phone" placeholder="+385 91 234 5678" />
              </div>
              <div class="form-group">
                  <label for="session-notes">Napomena (opcionalno)</label>
                  <textarea id="session-notes" rows="2" placeholder="Npr. Trebam pomoć s integralima..."></textarea>
              </div>
  
              <h2 class="mt-2">Način plaćanja</h2>
              <div style="display: flex; gap: 1rem; margin-bottom: 1rem;">
                  <label style="cursor: pointer; display: flex; align-items: center; gap: 0.5rem;">
                      <input type="radio" name="payment" value="STRIPE" checked />
                      💳 Kartica (Stripe)
                  </label>
                  <label style="cursor: pointer; display: flex; align-items: center; gap: 0.5rem;">
                      <input type="radio" name="payment" value="BANK_TRANSFER" />
                      🏦 Bankovni prijenos
                  </label>
              </div>
  
              <button id="checkout-btn" class="btn btn-primary btn-lg" style="width: 100%;">Rezerviraj termin</button>
              <p id="checkout-error" class="text-muted mt-1 hidden" style="color: #dc2626;"></p>
          </div>
      `;
  
      document.getElementById('checkout-btn').addEventListener('click', async () => {
          const name = document.getElementById('guest-name').value.trim();
          const email = document.getElementById('guest-email').value.trim();
          const phone = document.getElementById('guest-phone').value.trim();
          const notes = document.getElementById('session-notes').value.trim();
          const paymentMethod = document.querySelector('input[name="payment"]:checked').value;
          const errorEl = document.getElementById('checkout-error');
  
          // Validation
          if (!name || !email || !phone) {
              showError(errorEl, 'Ime, email i telefon su obavezni');
              return;
          }
  
          if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
              showError(errorEl, 'Unesite ispravnu email adresu');
              return;
          }
  
          const btn = document.getElementById('checkout-btn');
          btn.disabled = true;
          btn.textContent = 'Obrada...';
          hideError(errorEl);
  
          try {
              const result = await api.post('/storefront/checkout', {
                  reservationId: data.reservationId,
                  serviceCategoryId: data.serviceId,
                  guest: { name, email, phone },
                  sessionNotes: notes || null,
                  paymentMethodChoice: paymentMethod
              });
  
              if (result.stripeRedirectUrl) {
                  // Redirect to Stripe
                  window.location.href = result.stripeRedirectUrl;
              } else {
                  // Bank transfer or zero-price — show confirmation
                  renderConfirmation(container, result, data);
              }
          } catch (err) {
            renderCheckoutError(container, data);
          }
      });
  }
  
  function renderConfirmation(container, result, serviceData) {
      const messages = {
          'PENDING_PAYMENT': 'Račun s uputama za plaćanje poslan je na Vaš email. Uplatu očekujemo u roku od 48 sati.',
          'PAID': 'Vaš termin je potvrđen! Potvrda je poslana na Vaš email.'
      };
  
      const message = messages[result.status] || 'Rezervacija uspješna!';
  
      container.innerHTML = `
          <div class="success-box">
              <h2>✅ Rezervacija zaprimljena</h2>
              <p class="mt-2">${message}</p>
              ${result.benefitApplied ? `
                  <p class="mt-1 text-muted">Primijenjena pogodnost: ${escapeHtml(result.benefitApplied.type)}</p>
              ` : ''}
              <button class="btn btn-primary mt-2" onclick="location.reload()">Nova rezervacija</button>
          </div>
      `;
  }

  function renderCheckoutError(container, data) {
    container.innerHTML = `
        <div class="card">
              <h2>Rezervacija nije uspjela</h2>
              <p class="mt-2">Došlo je do pogreške pri rezervaciji. Vaša rezervacija termina možda je istekla. Molimo odaberite termin ponovno.</p>
              <button id="retry-btn" class="btn btn-primary mt-2">Odaberite termin ponovno</button>
          </div>
    `;
    document.getElementById('retry-btn').addEventListener('click', () => {
        navigateTo('availability', {
            serviceId: data.serviceId,
            serviceName: data.serviceName,
            servicePrice: data.servicePrice
        });
    });
  }
  
  function showError(el, msg) {
      el.textContent = msg;
      el.classList.remove('hidden');
  }
  
  function hideError(el) {
      el.classList.add('hidden');
  }