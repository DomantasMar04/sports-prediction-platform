import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_URL,
    headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

export const authService = {
    register: (data) => api.post('/auth/register', data),
    login: (data) => api.post('/auth/login', data),
    getMe: () => api.get('/auth/me'),
    getProfile: () => api.get('/auth/profile'),
    deleteMe: () => api.delete('/auth/me'),
};

export const matchService = {
    getAll: (leagueId, status) => {
        const params = {};
        if (leagueId) params.leagueId = leagueId;
        if (status) params.status = status;
        return api.get('/matches', { params });
    },
    getById: (id) => api.get(`/matches/${id}`),
    create: (match) => api.post('/matches', match),
    updateScore: (id, data) => api.patch(`/matches/${id}/score`, data),
};

export const predictionService = {
    create: (matchId, prediction) =>
        api.post(`/matches/${matchId}/predictions`, prediction),

    getMyPrediction: (matchId) =>
        api.get(`/matches/${matchId}/predictions/me`),

    update: (matchId, predictionId, data) =>
        api.patch(`/matches/${matchId}/predictions/${predictionId}`, data),

    delete: (matchId, predictionId) =>
        api.delete(`/matches/${matchId}/predictions/${predictionId}`),

    getUserPredictions: (userId) =>
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
    syncGames: () => api.post('/sync/games'),
    calculatePoints: (matchId) =>
        api.post(`/matches/${matchId}/predictions/calculate`),
};

export default api;