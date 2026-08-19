import { getToken, isAuthenticated, logout } from './auth.js';
  
  const BASE_URL = 'http://localhost:8080/api/v1';
  
  async function request(method, path, body = null) {
      const headers = {
          'Content-Type': 'application/json',
      };
  
      const token = getToken();
      if (token) {
          headers['Authorization'] = `Bearer ${token}`;
      }
  
      const options = { method, headers };
      if (body) {
          options.body = JSON.stringify(body);
      }
  
      const response = await fetch(`${BASE_URL}${path}`, options);
  
      if (response.status === 401 && isAuthenticated()) {
          logout();
          window.location.reload();
          return;
      }
  
      if (!response.ok) {
          const error = await response.json().catch(() => ({ message: 'Request failed' }));
          throw error;
      }
  
      if (response.status === 204) {
          return null;
      }
  
      return response.text().then(text => text ? JSON.parse(text) : null);
  }
  
  export const api = {
      get: (path) => request('GET', path),
      post: (path, body) => request('POST', path, body),
      put: (path, body) => request('PUT', path, body),
      patch: (path, body) => request('PATCH', path, body),
      delete: (path, body) => request('DELETE', path, body),
  };
