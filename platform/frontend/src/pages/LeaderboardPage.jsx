import { useState, useEffect } from 'react';
import { Container, Table, Nav, Badge } from 'react-bootstrap';
import { leaderboardService } from '../services/api';

function LeaderboardPage() {
    const [entries, setEntries] = useState([]);
    const [view, setView] = useState('global');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadLeaderboard();
    }, [view]);

    const loadLeaderboard = async () => {
        try {
            setLoading(true);
            const res = view === 'global'
                ? await leaderboardService.getGlobal()
                : await leaderboardService.getLeague(1);
            setEntries(res.data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const getMedal = (index) => {
        if (index === 0) return '🥇';
        if (index === 1) return '🥈';
        if (index === 2) return '🥉';
        return index + 1;
    };

    return (
        <Container className="mt-4">
            <h2 className="mb-4">Lyderių lentelė</h2>

            <Nav variant="tabs" className="mb-4">
                <Nav.Item>
                    <Nav.Link active={view === 'global'} onClick={() => setView('global')}>
                        Globali
                    </Nav.Link>
                </Nav.Item>
                <Nav.Item>
                    <Nav.Link active={view === 'league'} onClick={() => setView('league')}>
                        Mano lyga
                    </Nav.Link>
                </Nav.Item>
            </Nav>

            {loading ? (
                <p>Kraunama...</p>
            ) : (
                <Table striped bordered hover responsive>
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Vartotojas</th>
                        <th>Taškai</th>
                        <th>Spėjimai</th>
                    </tr>
                    </thead>
                    <tbody>
                    {entries.map((entry, index) => (
                        <tr key={entry.id}>
                            <td>{getMedal(index)}</td>
                            <td>{entry.user?.username}</td>
                            <td>
                                <Badge bg="primary">{entry.totalPoints}</Badge>
                            </td>
                            <td>{entry.predictionsCount}</td>
                        </tr>
                    ))}
                    {entries.length === 0 && (
                        <tr><td colSpan={4} className="text-center text-muted">Dar nėra duomenų</td></tr>
                    )}
                    </tbody>
                </Table>
            )}
        </Container>
    );
}

export default LeaderboardPage;