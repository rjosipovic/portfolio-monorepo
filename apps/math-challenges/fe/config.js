const PROTOCOL = 'http';
const HOST = 'localhost';

const API_ENDPOINTS = {
    // Base URLs
    CHALLENGE_MANAGER: `${PROTOCOL}://${HOST}:8080/`,
    USER_MANAGER: `${PROTOCOL}://${HOST}:8081/`,
    GAMIFICATION_MANAGER: `${PROTOCOL}://${HOST}:8082/`,
    NOTIFICATION_MANAGER: `${PROTOCOL}://${HOST}:8083/`,
    ANALYTICS_MANAGER: `${PROTOCOL}://${HOST}:8084/`,

    // Specific Paths
    get CHALLENGE_API() { return this.CHALLENGE_MANAGER + 'challenges'; },
    get ATTEMPT_API() { return this.CHALLENGE_MANAGER + 'attempts'; },

    get AUTH_API() { return this.USER_MANAGER + 'auth'; },
    get USERS_API() { return this.USER_MANAGER + 'users'; },

    get LEADERBOARD_API() { return this.GAMIFICATION_MANAGER + 'leaders'; },

    get CONTACT_API() { return this.NOTIFICATION_MANAGER + 'notifications'; },

    get STATS_API() { return this.ANALYTICS_MANAGER + 'statistics/user'; },
    get HISTORY_API() { return this.ANALYTICS_MANAGER + 'attempts'; }
};