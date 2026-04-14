import { useState, useEffect } from 'react';
import { Container, Card, Badge, Button, Row, Col } from 'react-bootstrap';
import { predictionService } from '../services/api';

const DEFAULT_USER_ID = 1;

function MyPredictionsPage() {
    const [predictions, setPredictions] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        predictionService.getUserPredictions(DEFAULT_USER_ID)
            .then(res => setPredictions(res.data))
            .catch(console.error)
            .finally(() => setLoading(false));
    }, []);

    const formatDateTime = (dt) => {
        if (!dt) return '';
        return new Date(dt).toLocaleString('lt-LT', {
            month: '2-digit', day: '2-digit',
            hour: '2-digit', minute: '2-digit'
        });
    };

    const getStatusBadge = (status) => {
        const map    = { UPCOMING: 'primary', LIVE: 'danger', FINISHED: 'secondary' };
        const labels = { UPCOMING: 'Būsimos', LIVE: 'Vyksta', FINISHED: 'Baigtos' };
        return <Badge bg={map[status] || 'secondary'}>{labels[status] || status}</Badge>;
    };

    const totalPoints = predictions.reduce((s, p) => s + (p.pointsEarned || 0), 0);
    const calculated = predictions.filter(p => p.isCalculated).length;

    if (loading) return <p className="mt-4 text-center">Kraunama...</p>;

    return (
        <Container className="mt-4">
            <h2 className="mb-2">Mano spėjimai</h2>

            {/* Suvestinė */}
            <div className="d-flex gap-3 mb-4">
                <Card className="flex-fill text-center py-2">
                    <div className="text-muted" style={{ fontSize: 12 }}>Viso spėjimų</div>
                    <div className="fw-bold fs-4">{predictions.length}</div>
                </Card>
                <Card className="flex-fill text-center py-2">
                    <div className="text-muted" style={{ fontSize: 12 }}>Suskaičiuota</div>
                    <div className="fw-bold fs-4">{calculated}</div>
                </Card>
                <Card className="flex-fill text-center py-2 border-success">
                    <div className="text-muted" style={{ fontSize: 12 }}>Iš viso taškų</div>
                    <div className="fw-bold fs-4 text-success">{totalPoints}</div>
                </Card>
            </div>

            {predictions.length === 0 && (
                <p className="text-muted">Dar nėra spėjimų. <a href="/matches">Spėti rungtynes →</a></p>
            )}

            <Row>
                {predictions.map(p => {
                    const match = p.match;
                    const homeName = match?.homeTeam?.name;
                    const awayName = match?.awayTeam?.name;
                    return (
                        <Col md={6} key={p.id} className="mb-3">
                            <Card className={p.isCalculated ? 'border-success' : ''}>
                                <Card.Body>
                                    <div className="d-flex justify-content-between align-items-center mb-2">
                                        {getStatusBadge(match?.status)}
                                        <small className="text-muted">{formatDateTime(match?.startTime)}</small>
                                    </div>

                                    {/* Komandos */}
                                    <div className="d-flex align-items-center justify-content-center gap-2 mb-2">
                                        {match?.homeTeam?.logoUrl && <img src={match.homeTeam.logoUrl} alt="" style={{ width: 24, height: 24, objectFit: 'contain' }} />}
                                        <span className="fw-semibold">{homeName}</span>
                                        <span className="text-muted">vs</span>
                                        <span className="fw-semibold">{awayName}</span>
                                        {match?.awayTeam?.logoUrl && <img src={match.awayTeam.logoUrl} alt="" style={{ width: 24, height: 24, objectFit: 'contain' }} />}
                                    </div>

                                    {/* Spėjimas */}
                                    <div className="text-center mb-2">
                                        <span className="text-muted" style={{ fontSize: 13 }}>Spėjimas: </span>
                                        <strong>{p.predictedHomeScore} : {p.predictedAwayScore}</strong>
                                        <span className="text-muted ms-2" style={{ fontSize: 13 }}>({p.predictedWinner})</span>
                                    </div>

                                    {/* Rezultatas jei baigta */}
                                    {match?.status === 'FINISHED' && (
                                        <div className="text-center mb-2 text-primary">
                                            Rezultatas: <strong>{match.homeScore} : {match.awayScore}</strong>
                                        </div>
                                    )}

                                    {/* Taškai */}
                                    {p.isCalculated ? (
                                        <div className="text-center">
                                            <Badge bg="success" className="fs-6">🏆 {p.pointsEarned} taškų</Badge>
                                        </div>
                                    ) : match?.status === 'FINISHED' ? (
                                        <div className="text-center text-muted" style={{ fontSize: 13 }}>Skaičiuojama...</div>
                                    ) : null}

                                    <div className="text-center mt-2">
                                        <Button variant="outline-secondary" size="sm" href={`/matches/${match?.id}`}>
                                            Peržiūrėti →
                                        </Button>
                                    </div>
                                </Card.Body>
                            </Card>
                        </Col>
                    );
                })}
            </Row>
        </Container>
    );
}

export default MyPredictionsPage;