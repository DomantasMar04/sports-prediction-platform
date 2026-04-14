import axios from 'axios';

const API_URL = 'http://localhost:8080/api';
const DEFAULT_USER_ID = 1;

const api = axios.create({
    baseURL: API_URL,
    headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

export const matchService = {
    getAll: (leagueId, status) => {
        const params = {};
        if (leagueId) params.leagueId = leagueId;
        if (status) params.status = status;
        return api.get('/matches', { params });
    },
    getById: (id) => api.get(`/matches/${id}`),
    create: (match) => api.post('/matches', match),
    updateScore: (id, data) => api.patch(`/matches/${id}/score`, data, {
        headers: { 'X-User-Role': 'ADMIN' }
    }),
};

export const predictionService = {
    create: (matchId, prediction, userId = DEFAULT_USER_ID) =>
        api.post(`/matches/${matchId}/predictions`, prediction, {
            headers: { 'X-User-Id': userId }
        }),

    getMyPrediction: (matchId, userId = DEFAULT_USER_ID) =>
        api.get(`/matches/${matchId}/predictions/me`, {
            headers: { 'X-User-Id': userId }
        }),

    update: (matchId, predictionId, data, userId = DEFAULT_USER_ID) =>
        api.patch(`/matches/${matchId}/predictions/${predictionId}`, data, {
            headers: { 'X-User-Id': userId }
        }),

    delete: (matchId, predictionId, userId = DEFAULT_USER_ID) =>
        api.delete(`/matches/${matchId}/predictions/${predictionId}`, {
            headers: { 'X-User-Id': userId }
        }),

    getUserPredictions: (userId = DEFAULT_USER_ID) =>
        api.get(`/users/${userId}/predictions`),
};

export const leaderboardService = {
    getLeague: (leagueId) =>
        api.get(`/leagues/${leagueId}/leaderboard`, {
            headers: { 'X-League-Type': 'CUSTOM' }
        }),
    getGlobal: () => api.get('/leaderboard/global'),
};

export const adminService = {
    syncGames: () =>
        api.post('/sync/games', null, {
            headers: { 'X-User-Role': 'ADMIN' }
        }),
    calculatePoints: (matchId) =>
        api.post(`/matches/${matchId}/predictions/calculate`, null, {
            headers: { 'X-User-Role': 'ADMIN' }
        }),
};

export default api;