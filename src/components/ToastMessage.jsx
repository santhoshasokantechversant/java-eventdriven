import React from 'react'
import { Toast, ToastContainer } from 'react-bootstrap'
import PropTypes from "prop-types";

/**
 * ToastMessage Component
 * -----------------------
 * A reusable toast notification component using React-Bootstrap.
 *
 * Props:
 * - show (bool): Controls the visibility of the toast.
 * - onClose (func): Callback fired when the toast is closed.
 * - message (string): Text displayed inside the toast.
 * - bg (string): Background color variant (default is "success").
 *
 * Features:
 * - Appears at the top-right corner of the screen.
 * - Auto-hides after 1500 milliseconds.
 * - Handles different background themes (success, danger, warning, etc.).
 */
export const ToastMessage = ({ show, onClose, message, bg = "success" }) => {
    return (
        <div>
            <ToastContainer position="top-end" className="p-3">
                <Toast
                    bg={bg}
                    onClose={onClose}
                    show={show}
                    delay={1500}
                    autohide
                >
                    <Toast.Body className="text-white">{message}</Toast.Body>
                </Toast>
            </ToastContainer>
        </div>
    )
};

// Add prop type validation 
ToastMessage.propTypes = {
    show: PropTypes.bool.isRequired,
    onClose: PropTypes.func.isRequired,
    message: PropTypes.string.isRequired,
    bg: PropTypes.string,
};

export default ToastMessage;
