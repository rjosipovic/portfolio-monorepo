import { isAuthenticated, logout, getToken } from './auth.js';
  import { renderLogin } from './pages/login.js';
  import { renderLanding } from './pages/landing.js';
  import { renderCalendar } from './pages/calendar.js';
  import { renderStudents } from './pages/students.js';
  import { renderFinance } from './pages/finance.js';
  
  const nav = document.getElementById('nav');
  const pageContainer = document.getElementById('page-container');
  const logoutBtn = document.getElementById('logout-btn');
  
  const pages = {
      landing: renderLanding,
      calendar: renderCalendar,
      students: renderStudents,
      finance: renderFinance,
  };
  
  function navigate(page) {
      if (!isAuthenticated()) {
          showLogin();
          return;
      }
  
      nav.classList.remove('hidden');
      document.querySelectorAll('.nav-links a').forEach(a => {
          a.classList.toggle('active', a.dataset.page === page);
      });
  
      const renderer = pages[page];
      if (renderer) {
          renderer(pageContainer);
      }
  }
  
  function showLogin() {
      nav.classList.add('hidden');
      renderLogin(pageContainer, () => navigate('landing'));
  }
  
  // Navigation clicks
  document.querySelectorAll('[data-page]').forEach(link => {
      link.addEventListener('click', (e) => {
          e.preventDefault();
          navigate(e.target.dataset.page);
      });
  });
  
  // Logout
  logoutBtn.addEventListener('click', () => {
      logout();
      showLogin();
  });
  
  // Init
  if (isAuthenticated()) {
      navigate('landing');
  } else {
      showLogin();
  }
