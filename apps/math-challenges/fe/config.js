const PROTOCOL = 'http';
const HOST = '127.0.0.1';

const API_ENDPOINTS = {
    // Base URLs
    BASE_URL: `${PROTOCOL}://${HOST}:8080/`,

    // Specific Paths
    get CHALLENGE_API() { return this.BASE_URL + 'challenges'; },
    get ATTEMPT_API() { return this.BASE_URL + 'attempts'; },

    get AUTH_API() { return this.BASE_URL + 'auth'; },
    get USERS_API() { return this.BASE_URL + 'users'; },

    get LEADERBOARD_API() { return this.BASE_URL + 'leaders'; },

    get CONTACT_API() { return this.BASE_URL + 'notifications'; },

    get STATS_API() { return this.BASE_URL + 'analytics/statistics'; },
    get HISTORY_API() { return this.BASE_URL + 'analytics/attempts'; }
};