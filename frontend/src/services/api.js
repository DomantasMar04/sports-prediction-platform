import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Automatiškai prideda JWT token prie kiekvienos užklausos
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Matches
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

// Predictions
export const predictionService = {
    create: (matchId, prediction, userId) =>
        api.post(`/matches/${matchId}/predictions`, prediction, {
            headers: { 'X-User-Id': userId }
        }),
    getMyPrediction: (matchId, userId) =>
        api.get(`/matches/${matchId}/predictions/me`, {
            headers: { 'X-User-Id': userId }
        }),
    update: (matchId, predictionId, data, userId) =>
        api.patch(`/matches/${matchId}/predictions/${predictionId}`, data, {
            headers: { 'X-User-Id': userId }
        }),
};

// Leaderboard
export const leaderboardService = {
    getLeague: (leagueId) =>
        api.get(`/leagues/${leagueId}/leaderboard`, {
            headers: { 'X-League-Type': 'CUSTOM' }
        }),
    getGlobal: () => api.get('/leaderboard/global'),
};

export default api;