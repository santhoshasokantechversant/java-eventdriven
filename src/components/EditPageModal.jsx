import { Modal } from 'react-bootstrap';
import PropTypes from "prop-types";

/**
 * EditPageModal Component
 *
 * This reusable modal is used for editing pages or displaying editable
 * forms inside a popup. The parent component controls the visibility,
 * the close action, and passes dynamic content through `children`.
 *
 * Props:
 * - show: Controls whether the modal is visible.
 * - handleClose: Callback for closing the modal.
 * - title: Title displayed at the top of the modal.
 * - children: The editable form or UI elements shown inside the modal body.
 */
export const EditPageModal = ({ show, handleClose, title, children }) => {
    return (
        <Modal show={show} onHide={handleClose} centered size='lg'>
            <Modal.Header closeButton>
                <Modal.Title className='fs-5'>{title}</Modal.Title>
            </Modal.Header>
            <Modal.Body>{children}</Modal.Body>
        </Modal>
    )
};

// Add prop type validation
EditPageModal.propTypes = {
    show: PropTypes.bool.isRequired,
    handleClose: PropTypes.func.isRequired,
    title: PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.node
    ]).isRequired,
    children: PropTypes.node.isRequired
};

export default EditPageModal;