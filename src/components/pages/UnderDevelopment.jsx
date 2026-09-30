import React from 'react'
import { Card } from 'react-bootstrap'

export const UnderDevelopment = () => {
    // Get sideNav from sessionStorage
    const sideNav = JSON.parse(sessionStorage.getItem("sideNav") || "[]");

    // Condition: empty array OR first element's slugName is empty
    const isEmpty =
        sideNav.length === 0 || !sideNav[0]?.slugName || sideNav[0].slugName.trim() === "";

    return (
        <>
            {isEmpty ? (
                // Render another div if no data or empty slugName
                <div className="d-flex justify-content-center align-items-center mt-25 bg-light">
                    <Card className="shadow-lg text-center p-4" style={{ maxWidth: "450px", borderRadius: "20px" }}>
                        <Card.Body>
                            <div className="mb-3 text-danger fw-bold">
                                <i className="bi bi-exclamation-triangle" style={{ fontSize: "3rem" }}></i>
                            </div>
                            <h4 className="fw-bold text-dark">No Privileges given</h4>
                            <p className="text-muted">
                                You are not given permission to access. Please contact the administrator.
                            </p>
                        </Card.Body>
                    </Card>
                </div>
            ) : (
                // 👉 Show your original UnderDevelopment card
                <div className="d-flex justify-content-center align-items-center mt-25 bg-light">
                    <Card className="shadow-lg text-center p-4" style={{ maxWidth: "450px", borderRadius: "20px" }}>
                        <Card.Body>
                            <div className="mb-3 text-warning fw-bold">
                                <i className="bi bi-cone-striped" style={{ fontSize: "3rem" }}></i>
                            </div>
                            <h4 className="fw-bold text-dark">Page Under Development</h4>
                            <p className="text-muted">
                                We’re working hard to bring you this feature soon.
                                Please check back later or return to the dashboard.
                            </p>
                            <button type='button'
                                className="btn btn-primary mt-3 px-4"
                                onClick={() => (window.location.href = "/dashboard")}
                            >
                                Back to Dashboard
                            </button>
                        </Card.Body>
                    </Card>
                </div>
            )}
        </>
    )
}
