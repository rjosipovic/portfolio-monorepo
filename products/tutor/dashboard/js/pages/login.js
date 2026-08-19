import { api } from '../api.js';
  import { setToken } from '../auth.js';
  import { escapeHtml } from '../utils.js';
  
  export function renderLogin(container, onSuccess) {
      container.innerHTML = `
          <div class="login-container">
              <div class="login-card">
                  <h1>Prijava</h1>
                  <div id="login-step-email">
                      <div class="form-group">
                          <label for="email">Email adresa</label>
                          <input type="email" id="email" placeholder="tutor@primjer.hr" />
                      </div>
                      <button id="request-otp-btn" class="btn btn-primary" style="width:100%">Pošalji kod</button>
                      <p id="email-error" class="text-muted mt-1 hidden" style="color: #dc2626;"></p>
                  </div>
  
                  <div id="login-step-otp" class="hidden">
                      <p class="mb-2">Jednokratni kod poslan na <strong id="sent-email"></strong></p>
                      <div class="form-group">
                          <label for="otp">Kod</label>
                          <input type="text" id="otp" placeholder="123456" maxlength="6" inputmode="numeric" pattern="[0-9]" />
                      </div>
                      <button id="verify-otp-btn" class="btn btn-primary" style="width:100%">Prijavi se</button>
                      <p id="otp-error" class="text-muted mt-1 hidden" style="color: #dc2626;"></p>
                      <p class="mt-2 text-muted" style="text-align: center;">
                        <a href="#" id="back-to-email" style="color: #2563eb;">← Novi kod</a>
                      </p>
                  </div>
              </div>
          </div>
      `;
  
      const emailStep = document.getElementById('login-step-email');
      const otpStep = document.getElementById('login-step-otp');
      const emailInput = document.getElementById('email');
      const otpInput = document.getElementById('otp');
      const requestBtn = document.getElementById('request-otp-btn');
      const verifyBtn = document.getElementById('verify-otp-btn');
      const emailError = document.getElementById('email-error');
      const otpError = document.getElementById('otp-error');
      const sentEmail = document.getElementById('sent-email');
  
      requestBtn.addEventListener('click', async () => {
          const email = emailInput.value.trim();
          if (!email) {
              showError(emailError, 'Unesite email adresu');
              return;
          }
  
          requestBtn.disabled = true;
          requestBtn.textContent = 'Slanje...';
          hideError(emailError);
  
          try {
              await api.post('/auth/otp/request', { email });
              sentEmail.textContent = email;
              emailStep.classList.add('hidden');
              otpStep.classList.remove('hidden');
              otpInput.focus();
          } catch (err) {
              showError(emailError, err.reason || 'Greška pri slanju koda');
          } finally {
              requestBtn.disabled = false;
              requestBtn.textContent = 'Pošalji kod';
          }
      });
  
      verifyBtn.addEventListener('click', async () => {
          const email = emailInput.value.trim();
          const otp = otpInput.value.trim();
          if (!otp) {
              showError(otpError, 'Unesite kod');
              return;
          }
          hideError(otpError);

          if (!/^\d{6}$/.test(otp)) {
            showError(otpError, 'Kod mora sadržavati 6 znamenki');
            return;
          }

          // Onyle disable afte all validation passes
          verifyBtn.disabled = true;
          verifyBtn.textContent = 'Provjera...';
  
          try {
              const result = await api.post('/auth/otp/verify', { email, otp });
              setToken(result.accessToken);
              onSuccess();
          } catch (err) {
              showError(otpError, err.reason || 'Neispravan kod');
          } finally {
              verifyBtn.disabled = false;
              verifyBtn.textContent = 'Prijavi se';
          }
      });

      document.getElementById('back-to-email').addEventListener('click', (e) => {
        e.preventDefault();
        otpStep.classList.add('hidden');
        emailStep.classList.remove('hidden');
        otpInput.value = '';
        hideError(otpError);
      });
  
      // Enter key support
      emailInput.addEventListener('keydown', (e) => {
          if (e.key === 'Enter') requestBtn.click();
      });
      otpInput.addEventListener('keydown', (e) => {
          if (e.key === 'Enter') verifyBtn.click();
      });
  }
  
  function showError(el, message) {
      el.textContent = message;
      el.classList.remove('hidden');
  }
  
  function hideError(el) {
      el.classList.add('hidden');
  }
