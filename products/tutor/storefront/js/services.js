import { api } from './api.js';
  import { formatCurrency, escapeHtml } from './utils.js';
  import { navigateTo } from './app.js';
  
  export async function renderServices(container) {
      container.innerHTML = '<p>Učitavanje...</p>';
  
      try {
          const services = await api.get('/storefront/services');
  
          container.innerHTML = `
              <div class="steps">
                  <div class="step active"><span class="step-number">1</span> Usluga</div>
                  <div class="step"><span class="step-number">2</span> Termin</div>
                  <div class="step"><span class="step-number">3</span> Rezervacija</div>
              </div>
  
              <h2 class="mb-2">Odaberite uslugu</h2>
  
              <div class="services-grid" id="services-grid">
                  ${services.map(s => `
                      <div class="service-card" data-service-id="${s.id}">
                          <h3>${escapeHtml(s.name)}</h3>
                          <p class="text-muted mb-1">${escapeHtml(s.description)}</p>
                          <p class="price">${formatCurrency(s.price)}</p>
                      </div>
                  `).join('')}
              </div>
          `;
  
          container.querySelectorAll('.service-card').forEach(card => {
              card.addEventListener('click', () => {
                  // Deselect all
                  container.querySelectorAll('.service-card').forEach(c => c.classList.remove('selected'));
                  // Select this one
                  card.classList.add('selected');
  
                  // Look up the service by id from the fetched list (avoids putting
                  // user-controlled name/price into HTML attributes)
                  const service = services.find(s => s.id === card.dataset.serviceId);
                  if (!service) return;
  
                  // Navigate to availability with selected service
                  setTimeout(() => {
                      navigateTo('availability', {
                          serviceId: service.id,
                          serviceName: service.name,
                          servicePrice: service.price
                      });
                  }, 300);
              });
          });
      } catch (err) {
          container.innerHTML = `<p style="color: #dc2626;">Greška pri učitavanju usluga</p>`;
      }
  }