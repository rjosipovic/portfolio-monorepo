import { api } from '../api.js';
  import { formatDate, formatTime, formatCurrency, escapeHtml } from '../utils.js';
  
  let currentView = 'list'; // 'list' | 'profile'
  let currentStudentId = null;
  
  export async function renderStudents(container) {
      currentView = 'list';
      currentStudentId = null;
      await renderStudentList(container);
  }
  
  async function renderStudentList(container) {
      container.innerHTML = `
          <h1 class="mb-2">Studenti</h1>
          <div class="card">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                  <input type="text" id="search-input" placeholder="Pretraži po imenu ili emailu..." style="width: 60%; padding: 0.5rem; border: 1px
  solid #d1d5db; border-radius: 4px;" />
                  <button id="new-student-btn" class="btn btn-primary">+ Novi student</button>
              </div>
              <div id="student-list"></div>
          </div>
          <div id="new-student-form" class="card hidden">
              <h2>Novi student</h2>
              <div class="form-group"><label>Ime</label><input type="text" id="new-name" /></div>
              <div class="form-group"><label>Email</label><input type="email" id="new-email" /></div>
              <div class="form-group"><label>Telefon</label><input type="tel" id="new-phone" /></div>
              <button id="save-student-btn" class="btn btn-primary">Spremi</button>
              <button id="cancel-student-btn" class="btn" style="margin-left: 0.5rem;">Odustani</button>
              <p id="new-student-error" class="text-muted mt-1 hidden" style="color: #dc2626;"></p>
          </div>
      `;
  
      await loadStudents('');
  
      document.getElementById('search-input').addEventListener('input', async (e) => {
          await loadStudents(e.target.value);
      });
  
      document.getElementById('new-student-btn').addEventListener('click', () => {
          resetNewStudentForm();
          document.getElementById('new-student-form').classList.remove('hidden');
      });
  
      document.getElementById('cancel-student-btn').addEventListener('click', () => {
        resetNewStudentForm();
          document.getElementById('new-student-form').classList.add('hidden');
      });
  
      document.getElementById('save-student-btn').addEventListener('click', async () => {
          const name = document.getElementById('new-name').value.trim();
          const email = document.getElementById('new-email').value.trim();
          const phone = document.getElementById('new-phone').value.trim();
          const errorEl = document.getElementById('new-student-error');
  
          if (!name || !email || !phone) {
              errorEl.textContent = 'Sva polja su obavezna';
              errorEl.classList.remove('hidden');
              return;
          }
  
          try {
              await api.post('/dashboard/students', { name, email, phone });
              document.getElementById('new-student-form').classList.add('hidden');
              resetNewStudentForm();
              await loadStudents('');
          } catch (err) {
              errorEl.textContent = err.reason || 'Greška pri spremanju';
              errorEl.classList.remove('hidden');
          }
      });
  }
  
  async function loadStudents(query) {
      const listEl = document.getElementById('student-list');
      try {
          const params = query ? `?query=${encodeURIComponent(query)}` : '';
          const students = await api.get(`/dashboard/students${params}`);
  
          if (students.length === 0) {
              listEl.innerHTML = '<p class="text-muted">Nema studenata</p>';
              return;
          }
  
          listEl.innerHTML = `
              <table>
                  <thead><tr><th>Ime</th><th>Email</th><th>Telefon</th><th></th></tr></thead>
                  <tbody>
                      ${students.map(s => `
                          <tr>
                              <td>${escapeHtml(s.name)}</td>
                              <td>${escapeHtml(s.email)}</td>
                              <td>${escapeHtml(s.phone)}</td>
                              <td><button class="btn btn-primary btn-sm" data-student-id="${s.id}">Profil</button></td>
                          </tr>
                      `).join('')}
                  </tbody>
              </table>
          `;
  
          listEl.querySelectorAll('[data-student-id]').forEach(btn => {
              btn.addEventListener('click', () => renderStudentProfile(document.getElementById('page-container'), btn.dataset.studentId));
          });
      } catch (err) {
          listEl.innerHTML = `<p style="color: #dc2626;">Greška pri učitavanju</p>`;
      }
  }
  
  async function renderStudentProfile(container, studentId) {
      currentView = 'profile';
      currentStudentId = studentId;
  
      container.innerHTML = '<p>Učitavanje...</p>';
  
      try {
          const [profile, appointments, notes, benefits] = await Promise.all([
              api.get(`/dashboard/students/${studentId}`),
              api.get(`/dashboard/students/${studentId}/appointments`),
              api.get(`/dashboard/students/${studentId}/notes`),
              api.get(`/dashboard/students/${studentId}/benefits`)
          ]);
  
          container.innerHTML = `
              <button id="back-to-list" class="btn mb-2">← Natrag</button>
              <h1 class="mb-2">${escapeHtml(profile.name)}</h1>
  
              <div class="card">
                  <h2>Podaci</h2>
                  <p><strong>Email:</strong> ${escapeHtml(profile.email)}</p>
                  <p><strong>Telefon:</strong> ${escapeHtml(profile.phone)}</p>
                  <p class="mt-1"><strong>Ukupno lekcija:</strong> ${profile.metrics?.totalLessonsCompleted ?? 0}</p>
                  <p><strong>Ukupni prihod:</strong> ${formatCurrency(profile.metrics?.totalRevenue)}</p>
                  <p><strong>Zadnja lekcija:</strong> ${formatDate(profile.metrics?.lastLessonDate)}</p>
              </div>
  
              <div class="card">
                  <h2>Povijest termina</h2>
                  ${renderAppointmentHistory(appointments)}
              </div>
  
              <div class="card">
                  <h2>Bilješke</h2>
                  <div id="notes-list">${renderNotes(notes)}</div>
                  <div class="mt-2">
                      <textarea id="new-note" rows="2" style="width:100%; padding:0.5rem; border:1px solid #d1d5db; border-radius:4px;"
  placeholder="Nova bilješka..."></textarea>
                      <button id="add-note-btn" class="btn btn-primary mt-1">Dodaj bilješku</button>
                  </div>
              </div>
  
              <div class="card">
                  <h2>Pogodnosti</h2>
                  <div id="benefits-list">${renderBenefits(benefits)}</div>
                  <div class="mt-2" style="display:flex; gap:0.5rem; align-items:end; flex-wrap:wrap;">
                      <select id="benefit-type" style="padding:0.5rem; border:1px solid #d1d5db; border-radius:4px;">
                          <option value="FREE_LESSON">Besplatna lekcija</option>
                          <option value="PERCENTAGE_DISCOUNT">Popust (%)</option>
                          <option value="FIXED_AMOUNT_OFF">Fiksni popust (€)</option>
                      </select>
                      <input type="number" id="benefit-value" placeholder="Vrijednost" style="width:100px; padding:0.5rem; border:1px solid #d1d5db;
  border-radius:4px;" />
                      <input type="text" id="benefit-note" placeholder="Napomena" style="padding:0.5rem; border:1px solid #d1d5db;
  border-radius:4px;" />
                      <button id="add-benefit-btn" class="btn btn-primary">Dodaj</button>
                  </div>
              </div>
          `;
  
          // Back button
          document.getElementById('back-to-list').addEventListener('click', () => renderStudents(container));
  
          // Add note
          document.getElementById('add-note-btn').addEventListener('click', async () => {
              const content = document.getElementById('new-note').value.trim();
              if (!content) return;
              try {
                  await api.post(`/dashboard/students/${studentId}/notes`, { content });
                  document.getElementById('new-note').value = '';
                  const updatedNotes = await api.get(`/dashboard/students/${studentId}/notes`);
                  document.getElementById('notes-list').innerHTML = renderNotes(updatedNotes);
              } catch (err) {
                  alert(err.reason || 'Greška');
              }
          });
  
          // Add benefit
          document.getElementById('add-benefit-btn').addEventListener('click', async () => {
              const benefitType = document.getElementById('benefit-type').value;
              const value = document.getElementById('benefit-value').value;
              const note = document.getElementById('benefit-note').value.trim();
  
              const body = { benefitType, note };
              if (benefitType !== 'FREE_LESSON') {
                  body.value = parseFloat(value);
              }
  
              try {
                  await api.post(`/dashboard/students/${studentId}/benefits`, body);
                  document.getElementById('benefit-value').value = '';
                  document.getElementById('benefit-note').value = '';
                  document.getElementById('benefit-type').selectedIndex = 0;
                  const updatedBenefits = await api.get(`/dashboard/students/${studentId}/benefits`);
                  document.getElementById('benefits-list').innerHTML = renderBenefits(updatedBenefits);
              } catch (err) {
                  alert(err.reason || 'Greška');
              }
          });
  
      } catch (err) {
          container.innerHTML = `<p style="color: #dc2626;">Greška: ${escapeHtml(err.message || 'Neuspjelo učitavanje')}</p>`;
      }
  }
  
  function renderAppointmentHistory(appointments) {
      if (!appointments || appointments.length === 0) {
          return '<p class="text-muted">Nema termina</p>';
      }
  
      const stateLabels = {
          'RESERVED': 'Rezerviran', 'PAID': 'Plaćen', 'PENDING_PAYMENT': 'Čeka uplatu',
          'CONFIRMED': 'Potvrđen', 'COMPLETED': 'Održan', 'NO_SHOW': 'Neostvaren',
          'CANCELLED': 'Otkazan', 'PRE_BOOKED': 'Direktan'
      };
  
      return `
          <table>
              <thead><tr><th>Datum</th><th>Vrijeme</th><th>Usluga</th><th>Status</th></tr></thead>
              <tbody>
                  ${appointments.map(a => `
                      <tr>
                          <td>${formatDate(a.date)}</td>
                          <td>${formatTime(a.startTime)}</td>
                          <td>${escapeHtml(a.serviceCategoryName)}</td>
                          <td>${stateLabels[a.state] || a.state}</td>
                      </tr>
                  `).join('')}
              </tbody>
          </table>
      `;
  }
  
  function renderNotes(notes) {
      if (!notes || notes.length === 0) {
          return '<p class="text-muted">Nema bilješki</p>';
      }
  
      return notes.map(n => `
          <div style="padding: 0.5rem; border-bottom: 1px solid #e5e7eb;">
              <p>${escapeHtml(n.content)}</p>
              <p class="text-muted">${formatDate(n.createdAt?.split('T')[0])}</p>
          </div>
      `).join('');
  }
  
  function renderBenefits(benefits) {
      if (!benefits || benefits.length === 0) {
          return '<p class="text-muted">Nema pogodnosti</p>';
      }
  
      const typeLabels = {
          'FREE_LESSON': 'Besplatna lekcija',
          'PERCENTAGE_DISCOUNT': 'Popust (%)',
          'FIXED_AMOUNT_OFF': 'Fiksni popust (€)'
      };
  
      return `
          <table>
              <thead><tr><th>Tip</th><th>Vrijednost</th><th>Napomena</th><th>Status</th><th>Datum</th></tr></thead>
              <tbody>
                  ${benefits.map(b => `
                      <tr>
                          <td>${typeLabels[b.type] || b.type}</td>
                          <td>${b.value != null ? b.value : '—'}</td>
                          <td>${escapeHtml(b.note) || '—'}</td>
                          <td>${b.consumed ? '✅ Iskorišteno' : '⏳ Aktivno'}</td>
                          <td>${formatDate(b.grantedAt?.split('T')[0])}</td>
                      </tr>
                  `).join('')}
              </tbody>
          </table>
      `;
  }

  function resetNewStudentForm() {
    document.getElementById('new-name').value = '';
    document.getElementById('new-email').value = '';
    document.getElementById('new-phone').value = '';
    const errorEl = document.getElementById('new-student-error');
    errorEl.textContent = '';
    errorEl.classList.add('hidden');
  }