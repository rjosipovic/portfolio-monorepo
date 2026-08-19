let token = null;
  
  export function setToken(jwt) {
      token = jwt;
  }
  
  export function getToken() {
      return token;
  }
  
  export function isAuthenticated() {
      return token !== null;
  }
  
  export function logout() {
      token = null;
  }
