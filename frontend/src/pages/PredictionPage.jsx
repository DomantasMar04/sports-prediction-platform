import { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { Container, Card, Form, Button, Alert, Badge } from 'react-bootstrap';
import { matchService, predictionService } from '../services/api';

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

function CountdownDisplay({ remaining }) {
    if (remaining === null) return null;
    if (remaining <= 0) return <Badge bg="danger" className="fs-6 p-2">🔴 Rungtynės prasidėjo — spėjimas uždarytas</Badge>;
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
        <Badge bg={isDeadline ? 'warning' : 'success'} text={isDeadline ? 'dark' : 'white'} className="fs-6 p-2">
            ⏱ Liko: {label}{isDeadline && ' — skubėk!'}
        </Badge>
    );
}

function TeamHeader({ logoUrl, name }) {
    return (
        <div className="d-flex flex-column align-items-center gap-1">
            {logoUrl && <img src={logoUrl} alt={name} style={{ width: 48, height: 48, objectFit: 'contain' }} />}
            <span className="fw-semibold text-center" style={{ fontSize: '0.9rem' }}>{name}</span>
        </div>
    );
}

function PredictionPage() {
    const { matchId } = useParams();
    const userId = localStorage.getItem('userId') || 1;
    const [match, setMatch] = useState(null);
    const [homeQuarters, setHomeQuarters] = useState(['', '', '', '']);
    const [awayQuarters, setAwayQuarters] = useState(['', '', '', '']);
    const [predictedWinner, setPredictedWinner] = useState('');
    const [saved, setSaved] = useState(false);
    const [error, setError] = useState('');

    const remaining = useCountdown(match?.startTime);
    const isLocked = remaining !== null && remaining <= 10 * 60 * 1000;
    const isFinished = match?.status === 'FINISHED';

    useEffect(() => {
        matchService.getById(matchId).then((res) => setMatch(res.data));
    }, [matchId]);

    const calcTotal = (quarters) => quarters.reduce((sum, q) => sum + (parseInt(q) || 0), 0);
    const homeTotal = calcTotal(homeQuarters);
    const awayTotal = calcTotal(awayQuarters);

    const updateQuarter = (team, index, value) => {
        if (team === 'home') {
            const u = [...homeQuarters]; u[index] = value; setHomeQuarters(u);
        } else {
            const u = [...awayQuarters]; u[index] = value; setAwayQuarters(u);
        }
    };

    const formatDateTime = (dt) => {
        if (!dt) return '';
        return new Date(dt).toLocaleString('lt-LT', {
            year: 'numeric', month: '2-digit', day: '2-digit',
            hour: '2-digit', minute: '2-digit'
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (isLocked) { setError('Spėjimo laikas baigėsi.'); return; }
        try {
            await predictionService.create(matchId, {
                predictedWinner,
                predictedMvp: null,
                predictedFirstScorer: null,
                predictedHomeScore: homeTotal,
                predictedAwayScore: awayTotal,
                match: { id: parseInt(matchId) },
                user: { id: parseInt(userId) },
            }, userId);
            setSaved(true);
            setError('');
        } catch (err) {
            setError('Klaida išsaugant spėjimą: ' + (err.response?.data?.message || ''));
        }
    };

    if (!match) return <p className="mt-4 text-center">Kraunama...</p>;

    const homeName   = match.homeTeam?.name;
    const awayName   = match.awayTeam?.name;
    const homeLogo   = match.homeTeam?.logoUrl;
    const awayLogo   = match.awayTeam?.logoUrl;

    return (
        <Container className="mt-4" style={{ maxWidth: '680px' }}>
            <h2 className="mb-4">Spėjimas</h2>

            {/* Rungtynių kortelė su logotipais */}
            <Card className="mb-3">
                <Card.Body className="text-center">
                    <div className="d-flex justify-content-center align-items-center gap-4 mb-2">
                        <TeamHeader logoUrl={homeLogo} name={homeName} />
                        <span className="text-muted fw-bold fs-4">vs</span>
                        <TeamHeader logoUrl={awayLogo} name={awayName} />
                    </div>
                    <small className="text-muted d-block mb-2">📅 {formatDateTime(match.startTime)}</small>
                    {isFinished ? (
                        <h5 className="text-primary mt-2">Rezultatas: {match.homeScore} : {match.awayScore}</h5>
                    ) : (
                        <div className="mt-2"><CountdownDisplay remaining={remaining} /></div>
                    )}
                </Card.Body>
            </Card>

            {isLocked && !isFinished && <Alert variant="warning">🔒 <strong>Spėjimas uždarytas</strong> — liko mažiau nei 10 minučių.</Alert>}
            {isFinished && <Alert variant="secondary">Šios rungtynės jau baigtos.</Alert>}
            {saved && <Alert variant="success">Spėjimas išsaugotas! ✅</Alert>}
            {error && <Alert variant="danger">{error}</Alert>}

            <Form onSubmit={handleSubmit}>
                <fieldset disabled={isLocked || isFinished}>

                    {/* Kėlinukų lentelė */}
                    <Card className="mb-3">
                        <Card.Body>
                            <div className="fw-semibold mb-3">Kėlinukų rezultatai</div>
                            <div style={{ overflowX: 'auto' }}>
                                <table className="table table-bordered text-center mb-0" style={{ minWidth: '420px' }}>
                                    <thead className="table-light">
                                    <tr>
                                        <th style={{ textAlign: 'left', width: '36%' }}>Komanda</th>
                                        <th>K1</th><th>K2</th><th>K3</th><th>K4</th>
                                        <th className="table-secondary">Viso</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {[
                                        { name: homeName, logo: homeLogo, quarters: homeQuarters, team: 'home', total: homeTotal },
                                        { name: awayName, logo: awayLogo, quarters: awayQuarters, team: 'away', total: awayTotal },
                                    ].map(({ name, logo, quarters, team, total }) => (
                                        <tr key={team}>
                                            <td style={{ textAlign: 'left', verticalAlign: 'middle' }}>
                                                <div className="d-flex align-items-center gap-2">
                                                    {logo && <img src={logo} alt={name} style={{ width: 24, height: 24, objectFit: 'contain' }} />}
                                                    <span className="fw-semibold">{name}</span>
                                                </div>
                                            </td>
                                            {quarters.map((q, i) => (
                                                <td key={i} style={{ padding: '4px 6px' }}>
                                                    <input
                                                        type="number"
                                                        min="0"
                                                        value={q}
                                                        onChange={(e) => updateQuarter(team, i, e.target.value)}
                                                        disabled={isLocked || isFinished}
                                                        className="form-control form-control-sm"
                                                        style={{ textAlign: 'center', minWidth: '52px' }}
                                                    />
                                                </td>
                                            ))}
                                            <td className="table-secondary fw-bold fs-5" style={{ verticalAlign: 'middle' }}>
                                                {total}
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        </Card.Body>
                    </Card>

                    {/* Laimėtojas */}
                    <Form.Group className="mb-4">
                        <Form.Label>Laimėtoja komanda</Form.Label>
                        <Form.Select value={predictedWinner} onChange={(e) => setPredictedWinner(e.target.value)} required>
                            <option value="">Pasirink...</option>
                            <option value={homeName}>{homeName}</option>
                            <option value={awayName}>{awayName}</option>
                        </Form.Select>
                    </Form.Group>

                    <Button variant={isLocked || isFinished ? 'secondary' : 'primary'} type="submit" className="w-100" disabled={isLocked || isFinished}>
                        {isLocked || isFinished ? '🔒 Spėjimas uždarytas' : 'Išsaugoti spėjimą'}
                    </Button>
                </fieldset>
            </Form>
        </Container>
    );
}

export default PredictionPage;