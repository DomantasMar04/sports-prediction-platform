import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { authService } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const isAuthenticated = !!user;

    const saveAuthData = (data) => {
        localStorage.setItem('token', data.token);

        setUser({
            id: data.id,
            username: data.username,
            email: data.email,
            role: data.role,
        });
    };

    const login = async (formData) => {
        const response = await authService.login(formData);
        saveAuthData(response.data);
        return response.data;
    };

    const register = async (formData) => {
        const response = await authService.register(formData);
        saveAuthData(response.data);
        return response.data;
    };

    const logout = () => {
        localStorage.removeItem('token');
        setUser(null);
    };

    const fetchMe = async () => {
        try {
            const response = await authService.getMe();
            setUser(response.data);
        } catch (error) {
            localStorage.removeItem('token');
            setUser(null);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const token = localStorage.getItem('token');

        if (!token) {
            setLoading(false);
            return;
        }

        fetchMe();
    }, []);

    const value = useMemo(() => ({
        user,
        loading,
        isAuthenticated,
        login,
        register,
        logout,
        setUser,
    }), [user, loading, isAuthenticated]);

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error('useAuth must be used inside AuthProvider');
    }

    return context;
}