import { useEffect, useState } from 'react';
import { Container, Card, Badge, Button, Row, Col, Modal, Form } from 'react-bootstrap';
import { authService } from '../services/api';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

function ProfilePage() {
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();
    const { logout } = useAuth();
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deleteText, setDeleteText] = useState('');
    const [deleting, setDeleting] = useState(false);
    const [deleteError, setDeleteError] = useState('');

    useEffect(() => {
        authService.getProfile()
            .then(res => setProfile(res.data))
            .catch(console.error)
            .finally(() => setLoading(false));
    }, []);

    const formatDateTime = (dt) => {
        if (!dt) return '';
        return new Date(dt).toLocaleString('lt-LT', {
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getStatusBadge = (status) => {
        const map = { UPCOMING: 'primary', LIVE: 'danger', FINISHED: 'secondary' };
        const labels = { UPCOMING: 'Būsimos', LIVE: 'Vyksta', FINISHED: 'Baigtos' };
        return <Badge bg={map[status] || 'secondary'}>{labels[status] || status}</Badge>;
    };

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const openDeleteModal = () => {
        setDeleteText('');
        setDeleteError('');
        setShowDeleteModal(true);
    };

    const closeDeleteModal = () => {
        if (deleting) return;
        setShowDeleteModal(false);
        setDeleteText('');
        setDeleteError('');
    };

    const handleDeleteAccount = async () => {
        if (deleteText !== 'delete') {
            setDeleteError('Norėdami patvirtinti, įrašykite "delete".');
            return;
        }

        try {
            setDeleting(true);
            setDeleteError('');
            await authService.deleteMe();
            logout();
            navigate('/login');
        } catch (err) {
            setDeleteError('Nepavyko ištrinti paskyros.');
        } finally {
            setDeleting(false);
        }
    };

    if (loading) return <p className="mt-4 text-center">Kraunama...</p>;
    if (!profile) return <p className="mt-4 text-center">Nepavyko užkrauti profilio.</p>;

    const predictions = profile.predictions || [];
    const calculated = predictions.filter(p => p.isCalculated).length;

    return (
        <Container className="mt-4">
            <h2 className="mb-3">Mano profilis</h2>

            <div className="d-flex justify-content-end gap-2 mb-4">
                <Button variant="outline-dark" onClick={handleLogout}>
                    Atsijungti
                </Button>
                <Button variant="outline-danger" onClick={openDeleteModal}>
                    Ištrinti paskyrą
                </Button>
            </div>

            <Card className="mb-4">
                <Card.Body>
                    <Row>
                        <Col md={4}>
                            <div><strong>Vartotojo vardas:</strong> {profile.username}</div>
                        </Col>
                        <Col md={4}>
                            <div><strong>El. paštas:</strong> {profile.email}</div>
                        </Col>
                        <Col md={4}>
                            <div><strong>Rolė:</strong> {profile.role}</div>
                        </Col>
                    </Row>
                </Card.Body>
            </Card>

            <div className="d-flex gap-3 mb-4">
                <Card className="flex-fill text-center py-2">
                    <div className="text-muted" style={{ fontSize: 12 }}>Viso spėjimų</div>
                    <div className="fw-bold fs-4">{profile.predictionsCount}</div>
                </Card>
                <Card className="flex-fill text-center py-2">
                    <div className="text-muted" style={{ fontSize: 12 }}>Suskaičiuota</div>
                    <div className="fw-bold fs-4">{calculated}</div>
                </Card>
                <Card className="flex-fill text-center py-2 border-success">
                    <div className="text-muted" style={{ fontSize: 12 }}>Iš viso taškų</div>
                    <div className="fw-bold fs-4 text-success">{profile.totalScore}</div>
                </Card>
            </div>

            {predictions.length === 0 && (
                <p className="text-muted">
                    Dar nėra spėjimų. <a href="/matches">Spėti rungtynes →</a>
                </p>
            )}

            <Row>
                {predictions.map((p) => (
                    <Col md={6} key={p.id} className="mb-3">
                        <Card className={p.isCalculated ? 'border-success' : ''}>
                            <Card.Body>
                                <div className="d-flex justify-content-between align-items-center mb-2">
                                    {getStatusBadge(p.matchStatus)}
                                    <small className="text-muted">{formatDateTime(p.startTime)}</small>
                                </div>

                                <div className="d-flex align-items-center justify-content-center gap-2 mb-2">
                                    <span className="fw-semibold">{p.homeTeam}</span>
                                    <span className="text-muted">vs</span>
                                    <span className="fw-semibold">{p.awayTeam}</span>
                                </div>

                                <div className="text-center mb-2">
                                    <span className="text-muted" style={{ fontSize: 13 }}>Spėjimas: </span>
                                    <strong>{p.predictedHomeScore} : {p.predictedAwayScore}</strong>
                                    <span className="text-muted ms-2" style={{ fontSize: 13 }}>
                                        ({p.predictedWinner})
                                    </span>
                                </div>

                                {p.matchStatus === 'FINISHED' && (
                                    <div className="text-center mb-2 text-primary">
                                        Rezultatas: <strong>{p.actualHomeScore} : {p.actualAwayScore}</strong>
                                    </div>
                                )}

                                {p.isCalculated ? (
                                    <div className="text-center">
                                        <Badge bg="success" className="fs-6">🏆 {p.pointsEarned} taškų</Badge>
                                    </div>
                                ) : p.matchStatus === 'FINISHED' ? (
                                    <div className="text-center text-muted" style={{ fontSize: 13 }}>
                                        Skaičiuojama...
                                    </div>
                                ) : null}

                                <div className="text-center mt-2">
                                    <Button variant="outline-secondary" size="sm" href={`/matches/${p.matchId}`}>
                                        Peržiūrėti →
                                    </Button>
                                </div>
                            </Card.Body>
                        </Card>
                    </Col>
                ))}
            </Row>

            <Modal show={showDeleteModal} onHide={closeDeleteModal} centered>
                <Modal.Header closeButton={!deleting}>
                    <Modal.Title>Ištrinti paskyrą</Modal.Title>
                </Modal.Header>

                <Modal.Body>
                    <p>Ar tikrai norite ištrinti paskyrą? Šis veiksmas yra negrįžtamas.</p>
                    <p>Norėdami patvirtinti, įrašykite <strong>delete</strong>.</p>

                    <Form.Control
                        type="text"
                        value={deleteText}
                        onChange={(e) => setDeleteText(e.target.value)}
                        placeholder='Įrašykite "delete"'
                        disabled={deleting}
                    />

                    {deleteError && (
                        <p className="text-danger mt-2 mb-0">{deleteError}</p>
                    )}
                </Modal.Body>

                <Modal.Footer>
                    <Button variant="secondary" onClick={closeDeleteModal} disabled={deleting}>
                        Atšaukti
                    </Button>
                    <Button
                        variant="danger"
                        onClick={handleDeleteAccount}
                        disabled={deleting || deleteText !== 'delete'}
                    >
                        {deleting ? 'Trinama...' : 'Ištrinti paskyrą'}
                    </Button>
                </Modal.Footer>
            </Modal>
        </Container>
    );
}

export default ProfilePage;