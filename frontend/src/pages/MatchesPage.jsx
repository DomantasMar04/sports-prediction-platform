import { useState, useEffect, useCallback } from 'react';
import { Container, Row, Col, Card, Badge, Button, Form } from 'react-bootstrap';
import { matchService } from '../services/api';

function useCountdown(startTime) {
    const getRemaining = useCallback(() => {
        if (!startTime) return null;
        return new Date(startTime) - new Date();
    }, [startTime]);
    const [remaining, setRemaining] = useState(getRemaining);
    useEffect(() => {
        const interval = setInterval(() => setRemaining(getRemaining()), 1000);
        return () => clearInterval(interval);
    }, [getRemaining]);
    return remaining;
}

function CountdownBadge({ startTime }) {
    const remaining = useCountdown(startTime);
    if (remaining === null) return null;
    if (remaining <= 0) return <Badge bg="danger" className="ms-2">🔴 Prasidėjo</Badge>;
    const totalSec = Math.floor(remaining / 1000);
    const days  = Math.floor(totalSec / 86400);
    const hours = Math.floor((totalSec % 86400) / 3600);
    const mins  = Math.floor((totalSec % 3600) / 60);
    const secs  = totalSec % 60;
    const isDeadline = remaining <= 10 * 60 * 1000;
    let label;
    if (days > 0)       label = `${days}d ${hours}h ${mins}m`;
    else if (hours > 0) label = `${hours}h ${mins}m ${secs}s`;
    else                label = `${mins}m ${secs}s`;
    return (
        <Badge bg={isDeadline ? 'warning' : 'success'} text={isDeadline ? 'dark' : 'white'} className="ms-2">
            ⏱ {label}
        </Badge>
    );
}

function TeamLogo({ logoUrl, name }) {
    if (!logoUrl) return <span className="fw-semibold">{name}</span>;
    return (
        <div className="d-flex align-items-center gap-2 justify-content-center">
            <img src={logoUrl} alt={name} style={{ width: 32, height: 32, objectFit: 'contain' }} />
            <span className="fw-semibold">{name}</span>
        </div>
    );
}

function MatchCard({ match }) {
    const remaining = useCountdown(match.startTime);
    const isLocked = remaining !== null && remaining <= 10 * 60 * 1000;

    const getStatusBadge = (status) => {
        const map    = { UPCOMING: 'primary', LIVE: 'danger', FINISHED: 'secondary' };
        const labels = { UPCOMING: 'Būsimos', LIVE: 'Vyksta', FINISHED: 'Baigtos' };
        return <Badge bg={map[status] || 'secondary'}>{labels[status] || status}</Badge>;
    };

    const formatDateTime = (dt) => {
        if (!dt) return '';
        return new Date(dt).toLocaleString('lt-LT', {
            year: 'numeric', month: '2-digit', day: '2-digit',
            hour: '2-digit', minute: '2-digit'
        });
    };

    return (
        <Card className={`h-100 ${isLocked && match.status === 'UPCOMING' ? 'border-warning' : ''}`}>
            <Card.Body className="d-flex flex-column">
                <div className="d-flex justify-content-between align-items-center mb-2">
                    {getStatusBadge(match.status)}
                    {match.status === 'UPCOMING' && <CountdownBadge startTime={match.startTime} />}
                </div>

                {/* Komandos su logotipais */}
                <div className="d-flex align-items-center justify-content-center gap-3 my-2">
                    <TeamLogo logoUrl={match.homeTeam?.logoUrl} name={match.homeTeam?.name} />
                    <span className="text-muted fw-bold">vs</span>
                    <TeamLogo logoUrl={match.awayTeam?.logoUrl} name={match.awayTeam?.name} />
                </div>

                {match.status === 'FINISHED' && (
                    <h4 className="text-center text-primary my-1">
                        {match.homeScore} : {match.awayScore}
                    </h4>
                )}

                <small className="text-muted text-center d-block mb-3">
                    📅 {formatDateTime(match.startTime)}
                </small>

                {isLocked && match.status === 'UPCOMING' && (
                    <div className="text-center mb-2">
                        <Badge bg="warning" text="dark">🔒 Spėjimas uždarytas</Badge>
                    </div>
                )}

                <div className="text-center mt-auto">
                    {match.status === 'UPCOMING' && !isLocked ? (
                        <Button variant="outline-primary" size="sm" href={`/matches/${match.id}`}>Spėti</Button>
                    ) : match.status === 'UPCOMING' && isLocked ? (
                        <Button variant="outline-secondary" size="sm" disabled>Spėjimas uždarytas</Button>
                    ) : match.status === 'LIVE' ? (
                        <Button variant="outline-danger" size="sm" href={`/matches/${match.id}`}>Vyksta</Button>
                    ) : match.status === 'FINISHED' ? (
                        <Button variant="outline-secondary" size="sm" href={`/matches/${match.id}`}>Peržiūrėti</Button>
                    ) : null}
                </div>
            </Card.Body>
        </Card>
    );
}

const LKL_LEAGUE_ID = 4478;

function MatchesPage() {
    const [matches, setMatches] = useState([]);
    const [status, setStatus] = useState('UPCOMING');
    const [loading, setLoading] = useState(true);
    const [syncing, setSyncing] = useState(false);
    const [refreshingResults, setRefreshingResults] = useState(false);

    useEffect(() => { loadMatches(); }, [status]);

    const loadMatches = async () => {
        try {
            setLoading(true);
            const res = await matchService.getAll(null, status || null);
            console.log('MATCHES RESPONSE:', res.data);
            setMatches(Array.isArray(res.data) ? res.data : []);
        } catch (err) {
            console.error('LOAD MATCHES ERROR:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleSyncMatches = async () => {
        try {
            setSyncing(true);
            await matchService.syncUpcoming(LKL_LEAGUE_ID);
            await loadMatches();
        } catch (err) {
            console.error('Sync error:', err);
            console.error('Response data:', err.response?.data);
            console.error('Status:', err.response?.status);

            alert(`Nepavyko atnaujinti rungtynių. Status: ${err.response?.status ?? 'nera'}`);
        } finally {
            setSyncing(false);
        }
    };

    const handleRefreshResults = async () => {
        try {
            setRefreshingResults(true);
            await matchService.refreshResults();
            await loadMatches();
        } catch (err) {
            console.error('Refresh results error:', err);
            alert('Nepavyko atnaujinti rezultatų.');
        } finally {
            setRefreshingResults(false);
        }
    };

    console.log('MATCHES STATE:', matches);
    console.log('SELECTED STATUS:', status);

    return (
        <Container className="mt-4">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="mb-0">Rungtynės</h2>

                <div className="d-flex gap-2">
                    <Button variant="outline-primary" onClick={handleSyncMatches} disabled={syncing}>
                        {syncing ? 'Atnaujinama...' : 'Atnaujinti'}
                    </Button>

                    <Button variant="outline-secondary" onClick={handleRefreshResults} disabled={refreshingResults}>
                        {refreshingResults ? 'Tikrinama...' : 'Atnaujinti rezultatus'}
                    </Button>
                </div>
            </div>
            <Form.Select className="mb-4 w-25" value={status} onChange={(e) => setStatus(e.target.value)}>
                <option value="UPCOMING">Būsimos</option>
                <option value="">Visos</option>
                <option value="LIVE">Vykstančios</option>
                <option value="FINISHED">Baigtos</option>
            </Form.Select>
            {loading ? (
                <p>Kraunama...</p>
            ) : matches.length === 0 ? (
                <p className="text-muted">Rungtynių nerasta.</p>
            ) : (
                <Row>
                    {matches.map((match) => (
                        <Col md={4} key={match.id} className="mb-3">
                            <MatchCard match={match} />
                        </Col>
                    ))}
                </Row>
            )}
        </Container>
    );
}

export default MatchesPage;