export function formatDate(dateStr) {
      if (!dateStr) return '—';
      const [year, month, day] = dateStr.split('-');
      return `${day}.${month}.${year}.`;
  }
  
  export function formatTime(timeStr) {
      if (!timeStr) return '—';
      return timeStr.substring(0, 5);
  }
  
  export function formatCurrency(amount) {
      if (amount == null) return '—';
      return `${Number(amount).toFixed(2)} €`;
  }
  
  export function escapeHtml(str) {
      if (!str) return '';
      const div = document.createElement('div');
      div.appendChild(document.createTextNode(str));
      return div.innerHTML;
  }
