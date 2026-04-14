import { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { Container, Card, Form, Button, Alert, Badge } from 'react-bootstrap';
import { matchService, predictionService } from '../services/api';

const DEFAULT_USER_ID = 1;

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
    if (remaining <= 0) return <Badge bg="danger" className="fs-6 p-2">🔴 Prasidėjo — spėjimas uždarytas</Badge>;
    const s = Math.floor(remaining / 1000);
    const days = Math.floor(s / 86400);
    const hours = Math.floor((s % 86400) / 3600);
    const mins = Math.floor((s % 3600) / 60);
    const secs = s % 60;
    const isDeadline = remaining <= 10 * 60 * 1000;
    const label = days > 0 ? `${days}d ${hours}h ${mins}m`
        : hours > 0 ? `${hours}h ${mins}m ${secs}s`
            : `${mins}m ${secs}s`;
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

function BreakdownCard({ breakdown }) {
    if (!breakdown) return null;
    let data;
    try { data = JSON.parse(breakdown); } catch { return null; }
    const rows = [
        { label: 'Laimėtojas', points: data.winner },
        { label: 'Namų komandos taškai', points: data.homeScore },
        { label: 'Svečių komandos taškai', points: data.awayScore },
        { label: 'Taškų skirtumas', points: data.diff },
        { label: 'Bendras rezultatyvumas', points: data.total },
        { label: '🎯 Tikslus rezultatas (bonus)', points: data.exactBonus },
    ];
    const total = Object.values(data).reduce((s, v) => s + (v || 0), 0);
    return (
        <Card className="mt-3 border-success">
            <Card.Body>
                <Card.Title className="fs-6 text-success">Taškų skaičiavimas</Card.Title>
                <table className="table table-sm mb-0">
                    <tbody>
                    {rows.map(r => r.points > 0 && (
                        <tr key={r.label}>
                            <td>{r.label}</td>
                            <td className="text-end fw-semibold text-success">+{r.points}</td>
                        </tr>
                    ))}
                    <tr className="table-success fw-bold">
                        <td>Iš viso</td>
                        <td className="text-end">{total} taškų</td>
                    </tr>
                    </tbody>
                </table>
            </Card.Body>
        </Card>
    );
}

function PredictionPage() {
    const { matchId } = useParams();
    const [match, setMatch] = useState(null);
    const [existingPrediction, setExistingPrediction] = useState(null);
    const [homeQuarters, setHomeQuarters] = useState(['', '', '', '']);
    const [awayQuarters, setAwayQuarters] = useState(['', '', '', '']);
    const [predictedWinner, setPredictedWinner] = useState('');
    const [isEditing, setIsEditing] = useState(false);
    const [saved, setSaved] = useState(false);
    const [deleted, setDeleted] = useState(false);
    const [error, setError] = useState('');

    const remaining = useCountdown(match?.startTime);
    const isLocked = remaining !== null && remaining <= 10 * 60 * 1000;
    const isFinished = match?.status === 'FINISHED';
    const isLive = match?.status === 'LIVE';

    useEffect(() => {
        matchService.getById(matchId).then(res => setMatch(res.data));
        predictionService.getMyPrediction(matchId, DEFAULT_USER_ID)
            .then(res => {
                if (res.data) {
                    setExistingPrediction(res.data);
                    // Užpildyti formos laukus
                    setPredictedWinner(res.data.predictedWinner || '');
                    // Paskirstome total į kėlinukus (neturime kėlinukų, tai tiesiog rodome total)
                }
            })
            .catch(() => {}); // 404 = nėra spėjimo
    }, [matchId]);

    const calcTotal = (quarters) => quarters.reduce((sum, q) => sum + (parseInt(q) || 0), 0);
    const homeTotal = calcTotal(homeQuarters);
    const awayTotal = calcTotal(awayQuarters);

    const updateQuarter = (team, index, value) => {
        if (team === 'home') { const u = [...homeQuarters]; u[index] = value; setHomeQuarters(u); }
        else { const u = [...awayQuarters]; u[index] = value; setAwayQuarters(u); }
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
            const data = {
                predictedWinner,
                predictedHomeScore: homeTotal,
                predictedAwayScore: awayTotal,
                match: { id: parseInt(matchId) },
                user: { id: DEFAULT_USER_ID },
            };
            if (existingPrediction && isEditing) {
                const res = await predictionService.update(matchId, existingPrediction.id, data, DEFAULT_USER_ID);
                setExistingPrediction(res.data);
            } else {
                const res = await predictionService.create(matchId, data, DEFAULT_USER_ID);
                setExistingPrediction(res.data);
            }
            setSaved(true); setIsEditing(false); setError('');
        } catch (err) {
            setError('Klaida: ' + (err.response?.data?.message || err.message));
        }
    };

    const handleDelete = async () => {
        if (!existingPrediction) return;
        if (!window.confirm('Ištrinti spėjimą?')) return;
        try {
            await predictionService.delete(matchId, existingPrediction.id, DEFAULT_USER_ID);
            setExistingPrediction(null);
            setDeleted(true);
            setPredictedWinner('');
            setHomeQuarters(['', '', '', '']);
            setAwayQuarters(['', '', '', '']);
        } catch (err) {
            setError('Klaida trinant: ' + (err.response?.data?.message || err.message));
        }
    };

    if (!match) return <p className="mt-4 text-center">Kraunama...</p>;

    const homeName = match.homeTeam?.name;
    const awayName = match.awayTeam?.name;
    const canEdit = !isLocked && !isFinished && !isLive;

    // Jei yra spėjimas ir neredaguojam — rodome peržiūrą
    if (existingPrediction && !isEditing) {
        return (
            <Container className="mt-4" style={{ maxWidth: '680px' }}>
                <h2 className="mb-4">Mano spėjimas</h2>

                <Card className="mb-3">
                    <Card.Body className="text-center">
                        <div className="d-flex justify-content-center align-items-center gap-4 mb-2">
                            <TeamHeader logoUrl={match.homeTeam?.logoUrl} name={homeName} />
                            <span className="text-muted fw-bold fs-4">vs</span>
                            <TeamHeader logoUrl={match.awayTeam?.logoUrl} name={awayName} />
                        </div>
                        <small className="text-muted d-block mb-2">📅 {formatDateTime(match.startTime)}</small>
                        {isFinished && (
                            <h5 className="text-primary mt-2">Rezultatas: {match.homeScore} : {match.awayScore}</h5>
                        )}
                        {!isFinished && !isLive && <div className="mt-2"><CountdownDisplay remaining={remaining} /></div>}
                        {isLive && <Badge bg="danger" className="fs-6 mt-2">🔴 Vyksta</Badge>}
                    </Card.Body>
                </Card>

                <Card className="mb-3">
                    <Card.Body>
                        <div className="fw-semibold mb-3">Tavo spėjimas</div>
                        <table className="table table-bordered text-center mb-0">
                            <thead className="table-light">
                            <tr>
                                <th style={{ textAlign: 'left' }}>Komanda</th>
                                <th>Taškai</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td style={{ textAlign: 'left' }}>
                                    <div className="d-flex align-items-center gap-2">
                                        {match.homeTeam?.logoUrl && <img src={match.homeTeam.logoUrl} alt="" style={{ width: 20, height: 20, objectFit: 'contain' }} />}
                                        <span>{homeName}</span>
                                    </div>
                                </td>
                                <td className="fw-bold">{existingPrediction.predictedHomeScore}</td>
                            </tr>
                            <tr>
                                <td style={{ textAlign: 'left' }}>
                                    <div className="d-flex align-items-center gap-2">
                                        {match.awayTeam?.logoUrl && <img src={match.awayTeam.logoUrl} alt="" style={{ width: 20, height: 20, objectFit: 'contain' }} />}
                                        <span>{awayName}</span>
                                    </div>
                                </td>
                                <td className="fw-bold">{existingPrediction.predictedAwayScore}</td>
                            </tr>
                            </tbody>
                        </table>
                        <div className="mt-2 text-muted">Laimėtojas: <strong>{existingPrediction.predictedWinner}</strong></div>
                        {existingPrediction.isCalculated && (
                            <div className="mt-2">
                                <Badge bg="success" className="fs-6">🏆 Gauta taškų: {existingPrediction.pointsEarned}</Badge>
                            </div>
                        )}
                    </Card.Body>
                </Card>

                {existingPrediction.isCalculated && <BreakdownCard breakdown={existingPrediction.breakdown} />}

                {deleted && <Alert variant="info">Spėjimas ištrintas.</Alert>}
                {error && <Alert variant="danger">{error}</Alert>}

                {canEdit && (
                    <div className="d-flex gap-2 mt-3">
                        <Button variant="outline-primary" className="flex-grow-1" onClick={() => { setIsEditing(true); setSaved(false); }}>
                            ✏️ Redaguoti
                        </Button>
                        <Button variant="outline-danger" onClick={handleDelete}>
                            🗑️ Ištrinti
                        </Button>
                    </div>
                )}
            </Container>
        );
    }

    // Forma (nauja arba redagavimas)
    return (
        <Container className="mt-4" style={{ maxWidth: '680px' }}>
            <h2 className="mb-4">{isEditing ? 'Redaguoti spėjimą' : 'Spėjimas'}</h2>

            <Card className="mb-3">
                <Card.Body className="text-center">
                    <div className="d-flex justify-content-center align-items-center gap-4 mb-2">
                        <TeamHeader logoUrl={match.homeTeam?.logoUrl} name={homeName} />
                        <span className="text-muted fw-bold fs-4">vs</span>
                        <TeamHeader logoUrl={match.awayTeam?.logoUrl} name={awayName} />
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
                <fieldset disabled={isLocked || isFinished || isLive}>
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
                                        { name: homeName, logo: match.homeTeam?.logoUrl, quarters: homeQuarters, team: 'home', total: homeTotal },
                                        { name: awayName, logo: match.awayTeam?.logoUrl, quarters: awayQuarters, team: 'away', total: awayTotal },
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
                                                    <input type="number" min="0" value={q}
                                                           onChange={(e) => updateQuarter(team, i, e.target.value)}
                                                           className="form-control form-control-sm"
                                                           style={{ textAlign: 'center', minWidth: '52px' }} />
                                                </td>
                                            ))}
                                            <td className="table-secondary fw-bold fs-5" style={{ verticalAlign: 'middle' }}>{total}</td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        </Card.Body>
                    </Card>

                    <Form.Group className="mb-4">
                        <Form.Label>Laimėtoja komanda</Form.Label>
                        <Form.Select value={predictedWinner} onChange={(e) => setPredictedWinner(e.target.value)} required>
                            <option value="">Pasirink...</option>
                            <option value={homeName}>{homeName}</option>
                            <option value={awayName}>{awayName}</option>
                        </Form.Select>
                    </Form.Group>

                    <div className="d-flex gap-2">
                        {isEditing && (
                            <Button variant="outline-secondary" className="flex-shrink-0" onClick={() => setIsEditing(false)}>
                                Atšaukti
                            </Button>
                        )}
                        <Button variant={isLocked || isFinished ? 'secondary' : 'primary'} type="submit"
                                className="flex-grow-1" disabled={isLocked || isFinished || isLive}>
                            {isLocked || isFinished ? '🔒 Spėjimas uždarytas' : isEditing ? '💾 Išsaugoti pakeitimus' : 'Išsaugoti spėjimą'}
                        </Button>
                    </div>
                </fieldset>
            </Form>
        </Container>
    );
}

export default PredictionPage;