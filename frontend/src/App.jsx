import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import AppNavbar from './components/Navbar';
import MatchesPage from './pages/MatchesPage';
import PredictionPage from './pages/PredictionPage';
import LeaderboardPage from './pages/LeaderboardPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';


function AppContent() {
    const location = useLocation();
    const hideNavbar = location.pathname === '/' || location.pathname === '/login' || location.pathname === '/register';

    return (
        <>
            {!hideNavbar && <AppNavbar />}

            <Routes>
                <Route path="/" element={<LoginPage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/matches" element={<MatchesPage />} />
                <Route path="/matches/:matchId" element={<PredictionPage />} />
                <Route path="/leaderboard" element={<LeaderboardPage />} />
                <Route path="/profile" element={<ProfilePage />} />
            </Routes>
        </>
    );
}

function App() {
    return (
        <BrowserRouter>
            <AppContent />
        </BrowserRouter>
    );
}

export default App;