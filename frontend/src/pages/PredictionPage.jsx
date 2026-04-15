import { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import { Container, Card, Form, Button, Alert, Badge } from 'react-bootstrap';
import { matchService, predictionService } from '../services/api';

const DEFAULT_USER_ID = 1;

// --- PAGALBINIAI KOMPONENTAI ---

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

// --- TAŠKŲ DETALIZACIJA (PATAISYTA) ---
function BreakdownCard({ breakdown }) {
    if (!breakdown) return null;
    let data;
    try {
        data = typeof breakdown === 'string' ? JSON.parse(breakdown) : breakdown;
    } catch { return null; }

    // Žodynas atitinkantis tavo Backend JSON raktus
    const labels = {
        winner: "Atspėtas nugalėtojas",
        home: "Namų komandos taškai",
        away: "Svečių komandos taškai",
        quarters: "Kėlinių spėjimas",
        totalSum: "Bendra taškų suma",
        difference: "Taškų skirtumas"
    };

    // Sukuriame sąrašą iš visų galimų kategorijų (rodome net jei 0)
    const rows = Object.entries(data).map(([key, value]) => ({
        label: labels[key] || key,
        points: value || 0
    }));

    const total = Object.values(data).reduce((s, v) => s + (v || 0), 0);

    return (
        <Card className="mt-3 border-success shadow-sm">
            <Card.Body>
                <Card.Title className="fs-6 text-success fw-bold border-bottom pb-2">
                    Taškų skaičiavimas
                </Card.Title>
                <table className="table table-sm mb-0">
                    <tbody>
                    {rows.map((r, idx) => (
                        <tr key={idx}>
                            <td className="text-muted">{r.label}</td>
                            <td className={`text-end fw-semibold ${r.points > 0 ? 'text-success' : 'text-muted'}`}>
                                {r.points > 0 ? `+${r.points}` : r.points}
                            </td>
                        </tr>
                    ))}
                    <tr className="table-success fw-bold border-top">
                        <td>Iš viso už rungtynes</td>
                        <td className="text-end text-dark">{total} taškų</td>
                    </tr>
                    </tbody>
                </table>
            </Card.Body>
        </Card>
    );
}

// --- PAGRINDINIS PUSLAPIS ---

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
                    setPredictedWinner(res.data.predictedWinner || '');
                }
            })
            .catch(() => {});
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

        // Paskaičiuojame kėlinių laimėtoją (logika tavo DB)
        let hWins = 0, aWins = 0;
        for(let i=0; i<4; i++) {
            const h = parseInt(homeQuarters[i]) || 0;
            const a = parseInt(awayQuarters[i]) || 0;
            if (h > a) hWins++; else if (a > h) aWins++;
        }
        const qWinner = hWins > aWins ? 1 : (aWins > hWins ? 2 : 0);

        try {
            const data = {
                predictedWinner,
                predictedHomeScore: homeTotal,
                predictedAwayScore: awayTotal,
                predictedMostQuartersWinner: qWinner,
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

    if (existingPrediction && !isEditing) {
        return (
            <Container className="mt-4" style={{ maxWidth: '680px' }}>
                <h2 className="mb-4 text-center">Mano spėjimas</h2>

                <Card className="mb-3 shadow-sm text-center">
                    <Card.Body>
                        <div className="d-flex justify-content-center align-items-center gap-4 mb-2">
                            <TeamHeader logoUrl={match.homeTeam?.logoUrl} name={homeName} />
                            <span className="text-muted fw-bold fs-4">vs</span>
                            <TeamHeader logoUrl={match.awayTeam?.logoUrl} name={awayName} />
                        </div>
                        <small className="text-muted d-block mb-2">📅 {formatDateTime(match.startTime)}</small>
                        {isFinished && (
                            <div className="mt-2">
                                <span className="text-muted small">Rezultatas:</span>
                                <h3 className="text-primary">{match.homeScore} : {match.awayScore}</h3>
                            </div>
                        )}
                        {!isFinished && !isLive && <div className="mt-2"><CountdownDisplay remaining={remaining} /></div>}
                        {isLive && <Badge bg="danger" className="fs-6 mt-2">🔴 Vyksta</Badge>}
                    </Card.Body>
                </Card>

                <Card className="mb-3 shadow-sm">
                    <Card.Body>
                        <div className="fw-semibold mb-3 border-bottom pb-2">Tavo spėjimo detalės</div>
                        <div className="d-flex justify-content-between mb-2">
                            <span>Spėtas nugalėtojas:</span>
                            <strong className="text-primary">{existingPrediction.predictedWinner}</strong>
                        </div>
                        <div className="d-flex justify-content-between mb-3">
                            <span>Spėtas rezultatas:</span>
                            <strong className="fs-5">{existingPrediction.predictedHomeScore} : {existingPrediction.predictedAwayScore}</strong>
                        </div>

                        {existingPrediction.isCalculated ? (
                            <div className="text-center p-3 bg-light rounded">
                                <div className="text-muted small mb-1">Gauta taškų</div>
                                <Badge bg="success" className="fs-5 px-4">🏆 {existingPrediction.pointsEarned}</Badge>
                            </div>
                        ) : isFinished ? (
                            <Alert variant="info" className="text-center py-2 mb-0">Taškai skaičiuojami...</Alert>
                        ) : null}

                        {/* ČIA RODOMAS BREAKDOWN */}
                        {existingPrediction.isCalculated && (
                            <BreakdownCard breakdown={existingPrediction.breakdown} />
                        )}
                    </Card.Body>
                </Card>

                {deleted && <Alert variant="info" className="mt-3">Spėjimas ištrintas.</Alert>}
                {error && <Alert variant="danger" className="mt-3">{error}</Alert>}

                {canEdit && (
                    <div className="d-flex gap-2 mt-4">
                        <Button variant="outline-primary" className="flex-grow-1 py-2" onClick={() => { setIsEditing(true); setSaved(false); }}>
                            ✏️ Redaguoti spėjimą
                        </Button>
                        <Button variant="outline-danger" onClick={handleDelete}>🗑️</Button>
                    </div>
                )}
            </Container>
        );
    }

    return (
        <Container className="mt-4" style={{ maxWidth: '680px' }}>
            <h2 className="mb-4 text-center">{isEditing ? 'Redaguoti spėjimą' : 'Pateikti spėjimą'}</h2>

            <Card className="mb-3 border-0 bg-light text-center">
                <Card.Body>
                    <div className="d-flex justify-content-center align-items-center gap-4 mb-2">
                        <TeamHeader logoUrl={match.homeTeam?.logoUrl} name={homeName} />
                        <span className="text-muted fw-bold fs-4">vs</span>
                        <TeamHeader logoUrl={match.awayTeam?.logoUrl} name={awayName} />
                    </div>
                    {!isFinished && <CountdownDisplay remaining={remaining} />}
                </Card.Body>
            </Card>

            {isLocked && !isFinished && <Alert variant="warning">🔒 Spėjimas uždarytas (liko mažiau nei 10 min).</Alert>}
            {saved && <Alert variant="success">Spėjimas išsaugotas! ✅</Alert>}
            {error && <Alert variant="danger">{error}</Alert>}

            <Form onSubmit={handleSubmit}>
                <fieldset disabled={isLocked || isFinished || isLive}>
                    <Card className="mb-4 shadow-sm">
                        <Card.Body>
                            <Card.Title className="fs-6 mb-3">Kėlinukų spėjimas</Card.Title>
                            <div className="table-responsive">
                                <table className="table table-bordered text-center align-middle mb-0">
                                    <thead className="table-light">
                                    <tr>
                                        <th className="text-start" style={{ width: '40%' }}>Komanda</th>
                                        <th>K1</th><th>K2</th><th>K3</th><th>K4</th>
                                        <th className="table-secondary">Viso</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr>
                                        <td className="text-start fw-semibold">{homeName}</td>
                                        {[0,1,2,3].map(i => (
                                            <td key={i} className="p-1">
                                                <Form.Control type="number" size="sm" className="text-center border-0"
                                                              value={homeQuarters[i]} onChange={(e) => updateQuarter('home', i, e.target.value)} />
                                            </td>
                                        ))}
                                        <td className="table-secondary fw-bold">{homeTotal}</td>
                                    </tr>
                                    <tr>
                                        <td className="text-start fw-semibold">{awayName}</td>
                                        {[0,1,2,3].map(i => (
                                            <td key={i} className="p-1">
                                                <Form.Control type="number" size="sm" className="text-center border-0"
                                                              value={awayQuarters[i]} onChange={(e) => updateQuarter('away', i, e.target.value)} />
                                            </td>
                                        ))}
                                        <td className="table-secondary fw-bold">{awayTotal}</td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </Card.Body>
                    </Card>

                    <Form.Group className="mb-4">
                        <Form.Label className="fw-bold">Kas laimės rungtynes?</Form.Label>
                        <Form.Select size="lg" value={predictedWinner} onChange={(e) => setPredictedWinner(e.target.value)} required>
                            <option value="">Pasirinkite nugalėtoją...</option>
                            <option value={homeName}>{homeName}</option>
                            <option value={awayName}>{awayName}</option>
                        </Form.Select>
                    </Form.Group>

                    <div className="d-flex gap-2">
                        {isEditing && <Button variant="outline-secondary" onClick={() => setIsEditing(false)}>Atšaukti</Button>}
                        <Button variant="primary" type="submit" className="flex-grow-1 py-2 fs-5">
                            {isEditing ? '💾 Išsaugoti pakeitimus' : '🚀 Išsaugoti spėjimą'}
                        </Button>
                    </div>
                </fieldset>
            </Form>
        </Container>
    );
}

export default PredictionPage;