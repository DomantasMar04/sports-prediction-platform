import { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Badge, Button, Form } from 'react-bootstrap';
import { matchService } from '../services/api';

function MatchesPage() {
    const [matches, setMatches] = useState([]);
    const [status, setStatus] = useState('');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadMatches();
    }, [status]);

    const loadMatches = async () => {
        try {
            setLoading(true);
            const res = await matchService.getAll(null, status || null);
            setMatches(res.data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const getStatusBadge = (status) => {
        const map = {
            UPCOMING: 'primary',
            LIVE: 'danger',
            FINISHED: 'secondary',
        };
        return <Badge bg={map[status] || 'secondary'}>{status}</Badge>;
    };

    return (
        <Container className="mt-4">
            <h2 className="mb-4">Rungtynės</h2>

            <Form.Select
                className="mb-4 w-25"
                value={status}
                onChange={(e) => setStatus(e.target.value)}
            >
                <option value="">Visos</option>
                <option value="UPCOMING">Būsimos</option>
                <option value="LIVE">Vykstančios</option>
                <option value="FINISHED">Baigtos</option>
            </Form.Select>

            {loading ? (
                <p>Kraunama...</p>
            ) : (
                <Row>
                    {matches.map((match) => (
                        <Col md={4} key={match.id} className="mb-3">
                            <Card>
                                <Card.Body>
                                    <div className="d-flex justify-content-between align-items-center mb-2">
                                        {getStatusBadge(match.status)}
                                        <small className="text-muted">
                                            {new Date(match.startTime).toLocaleDateString('lt-LT')}
                                        </small>
                                    </div>
                                    <Card.Title className="text-center">
                                        {match.homeTeam?.name} vs {match.awayTeam?.name}
                                    </Card.Title>
                                    {match.status === 'FINISHED' && (
                                        <h4 className="text-center text-primary">
                                            {match.homeScore} : {match.awayScore}
                                        </h4>
                                    )}
                                    <div className="text-center mt-2">
                                        <Button variant="outline-primary" size="sm" href={`/matches/${match.id}`}>
                                            Spėti
                                        </Button>
                                    </div>
                                </Card.Body>
                            </Card>
                        </Col>
                    ))}
                    {matches.length === 0 && <p className="text-muted">Rungtynių nerasta.</p>}
                </Row>
            )}
        </Container>
    );
}

export default MatchesPage;