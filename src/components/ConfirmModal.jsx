import { Button, Modal } from "react-bootstrap";
import PropTypes from "prop-types";

/**
 * ConfirmModal Component
 *
 * This reusable modal is used to display a confirmation dialog,
 * typically for delete operations. The parent component controls
 * the visibility and provides callback functions for confirming
 * or cancelling the action.
 */
export const ConfirmModal = ({ show, handleClose, handleConfirm, title, body }) => {
    return (
        <div className="container mt-3">
            <Modal show={show} onHide={handleClose} centered>
                <Modal.Header closeButton className="d-flex justify-content-center text-black">
                    <Modal.Title className="text-center w-100">{title}</Modal.Title>
                </Modal.Header>

                <Modal.Body>
                    <p className="mb-0 p-1">{body}</p>
                </Modal.Body>

                <Modal.Footer>
                    <Button
                        variant="danger"
                        onClick={() => {
                            handleConfirm();
                            handleClose();
                        }}
                    >
                        Confirm
                    </Button>
                    <Button variant="secondary" onClick={handleClose}>
                        Cancel
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>

    );
};

// Add prop types validation
ConfirmModal.propTypes = {
    show: PropTypes.bool.isRequired,
    handleClose: PropTypes.func.isRequired,
    handleConfirm: PropTypes.func.isRequired,
    title: PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.node
    ]).isRequired,
    body: PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.node
    ]).isRequired
};

export default ConfirmModal;