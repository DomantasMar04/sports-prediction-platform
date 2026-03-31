import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Container, Card, Form, Button, Alert, Row, Col } from 'react-bootstrap';
import { matchService, predictionService } from '../services/api';

function PredictionPage() {
    const { matchId } = useParams();
    const userId = localStorage.getItem('userId') || 1;
    const [match, setMatch] = useState(null);
    const [prediction, setPrediction] = useState({
        predictedWinner: '',
        predictedFirstScorer: '',
        predictedMvp: '',
        predictedHomeScore: '',
        predictedAwayScore: '',
    });
    const [saved, setSaved] = useState(false);
    const [error, setError] = useState('');
    // const userId = localStorage.getItem('userId');

    useEffect(() => {
        matchService.getById(matchId).then((res) => setMatch(res.data));
    }, [matchId]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await predictionService.create(matchId, {
                predictedWinner: prediction.predictedWinner,
                predictedFirstScorer: prediction.predictedFirstScorer,
                predictedMvp: prediction.predictedMvp,
                predictedHomeScore: parseInt(prediction.predictedHomeScore),
                predictedAwayScore: parseInt(prediction.predictedAwayScore),
                match: { id: parseInt(matchId) },
                user: { id: parseInt(userId) },
            }, userId);
            setSaved(true);
            setError('');
        } catch (err) {
            console.error(err.response?.data);
            setError('Klaida išsaugant spėjimą: ' + (err.response?.data?.message || ''));
        }
    };

    if (!match) return <p className="mt-4 text-center">Kraunama...</p>;

    return (
        <Container className="mt-4" style={{ maxWidth: '600px' }}>
            <h2 className="mb-4">Spėjimas</h2>
            <Card className="mb-4">
                <Card.Body className="text-center">
                    <h4>{match.homeTeam?.name} vs {match.awayTeam?.name}</h4>
                    <small className="text-muted">
                        {new Date(match.startTime).toLocaleString('lt-LT')}
                    </small>
                </Card.Body>
            </Card>

            {saved && <Alert variant="success">Spėjimas išsaugotas! ✅</Alert>}
            {error && <Alert variant="danger">{error}</Alert>}

            <Form onSubmit={handleSubmit}>
                <Row className="mb-3">
                    <Col>
                        <Form.Label>Namų komandos taškai</Form.Label>
                        <Form.Control
                            type="number"
                            value={prediction.predictedHomeScore}
                            onChange={(e) => setPrediction({ ...prediction, predictedHomeScore: e.target.value })}
                            required
                        />
                    </Col>
                    <Col>
                        <Form.Label>Svečių komandos taškai</Form.Label>
                        <Form.Control
                            type="number"
                            value={prediction.predictedAwayScore}
                            onChange={(e) => setPrediction({ ...prediction, predictedAwayScore: e.target.value })}
                            required
                        />
                    </Col>
                </Row>

                <Form.Group className="mb-3">
                    <Form.Label>Laimėtoja komanda</Form.Label>
                    <Form.Select
                        value={prediction.predictedWinner}
                        onChange={(e) => setPrediction({ ...prediction, predictedWinner: e.target.value })}
                        required
                    >
                        <option value="">Pasirink...</option>
                        <option value={match.homeTeam?.name}>{match.homeTeam?.name}</option>
                        <option value={match.awayTeam?.name}>{match.awayTeam?.name}</option>
                    </Form.Select>
                </Form.Group>

                <Form.Group className="mb-3">
                    <Form.Label>Pirmieji taškai (žaidėjas)</Form.Label>
                    <Form.Control
                        type="text"
                        placeholder="Pvz. Jokić"
                        value={prediction.predictedFirstScorer}
                        onChange={(e) => setPrediction({ ...prediction, predictedFirstScorer: e.target.value })}
                    />
                </Form.Group>

                <Form.Group className="mb-3">
                    <Form.Label>MVP</Form.Label>
                    <Form.Control
                        type="text"
                        placeholder="Pvz. Dončić"
                        value={prediction.predictedMvp}
                        onChange={(e) => setPrediction({ ...prediction, predictedMvp: e.target.value })}
                    />
                </Form.Group>

                <Button variant="primary" type="submit" className="w-100">
                    Išsaugoti spėjimą
                </Button>
            </Form>
        </Container>
    );
}

export default PredictionPage;