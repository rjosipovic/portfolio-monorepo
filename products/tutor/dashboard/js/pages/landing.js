import { api } from '../api.js';
import { formatDate, formatTime, formatCurrency, escapeHtml } from '../utils.js';
import { closeAppointment } from '../actions/appointmentActions.js';

  
  export async function renderLanding(container) {
      container.innerHTML = '<p>Učitavanje...</p>';
  
      try {
          const data = await api.get('/dashboard/landing');
          container.innerHTML = `
              <h1 class="mb-2">Pregled</h1>
  
              <div class="card">
                  <h2>⏳ Čekaju zatvaranje</h2>
                  ${renderActionItems(data.awaitingClosure, 'closure')}
              </div>
  
              <div class="card">
                  <h2>💳 Čekaju uplatu</h2>
                  ${renderActionItems(data.pendingPayments, 'payment')}
              </div>
  
              <div class="card">
                  <h2>📅 Danas</h2>
                  ${renderActionItems(data.todayUpcoming, 'upcoming')}
              </div>
          `;
  
          attachClosureHandlers(container);
      } catch (err) {
          container.innerHTML = `<p style="color: #dc2626;">Greška pri učitavanju</p>`;
      }
  }
  
  function renderActionItems(items, type) {
      if (!items || items.length === 0) {
          return '<p class="text-muted">Nema stavki</p>';
      }
  
      const rows = items.map(item => `
          <tr>
              <td>${escapeHtml(item.studentName)}</td>
              <td>${escapeHtml(item.serviceCategory)}</td>
              <td>${formatDate(item.date)}</td>
              <td>${formatTime(item.startTime)}</td>
              <td>${formatCurrency(item.amount)}</td>
              ${type === 'closure' ? `
                  <td>
                      <button class="btn btn-success btn-sm" data-action="complete" data-id="${item.appointmentId}">Održan</button>
                      <button class="btn btn-danger btn-sm" data-action="noshow" data-id="${item.appointmentId}">Neostvaren</button>
                  </td>
              ` : ''}
          </tr>
      `).join('');
  
      return `
          <table>
              <thead>
                  <tr>
                      <th>Student</th>
                      <th>Usluga</th>
                      <th>Datum</th>
                      <th>Vrijeme</th>
                      <th>Iznos</th>
                      ${type === 'closure' ? '<th>Akcija</th>' : ''}
                  </tr>
              </thead>
              <tbody>${rows}</tbody>
          </table>
      `;
  }
  
  function attachClosureHandlers(container) {
      container.querySelectorAll('[data-action="complete"]').forEach(btn => {
          btn.addEventListener('click', () => closeAppointment(btn.dataset.id, 'COMPLETED', () => renderLanding(container)));
      });
  
      container.querySelectorAll('[data-action="noshow"]').forEach(btn => {
          btn.addEventListener('click', () => closeAppointment(btn.dataset.id, 'NO_SHOW', () => renderLanding(container)));
      });
  }