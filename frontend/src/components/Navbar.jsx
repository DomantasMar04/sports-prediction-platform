import { Navbar, Nav, Container } from 'react-bootstrap';

function AppNavbar() {
    return (
        <Navbar bg="dark" variant="dark" expand="lg">
            <Container>
                <Navbar.Brand href="/matches">🏆 Prediction Game</Navbar.Brand>
                <Navbar.Toggle />
                <Navbar.Collapse>
                    <Nav className="me-auto">
                        <Nav.Link href="/matches">Rungtynės</Nav.Link>
                        <Nav.Link href="/leaderboard">Lyderiai</Nav.Link>
                        <Nav.Link href="/profile">Profilis</Nav.Link>
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
}

export default AppNavbar;