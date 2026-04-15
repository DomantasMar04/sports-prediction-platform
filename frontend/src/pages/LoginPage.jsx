import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function LoginPage() {
    const navigate = useNavigate();
    const { login } = useAuth();

    const [formData, setFormData] = useState({
        email: '',
        password: '',
    });

    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await login(formData);
            navigate('/matches');
        } catch (err) {
            setError('Login failed. Check your email and password.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: '400px', margin: '40px auto' }}>
            <h2>Login</h2>

            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '12px' }}>
                    <label>Email</label>
                    <input
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        required
                        style={{ width: '100%', padding: '8px', marginTop: '4px' }}
                    />
                </div>

                <div style={{ marginBottom: '12px' }}>
                    <label>Password</label>
                    <input
                        type="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        required
                        style={{ width: '100%', padding: '8px', marginTop: '4px' }}
                    />
                </div>

                {error && (
                    <p style={{ color: 'red', marginBottom: '12px' }}>
                        {error}
                    </p>
                )}

                <button
                    type="submit"
                    disabled={loading}
                    style={{ width: '100%', padding: '10px' }}
                >
                    {loading ? 'Logging in...' : 'Login'}
                </button>
            </form>

            <p style={{ marginTop: '16px' }}>
                Don&apos;t have an account? <Link to="/register">Register</Link>
            </p>
        </div>
    );
}