import axios from "axios";
import { useEffect, useState } from "react";
import { Button, Modal } from "react-bootstrap";
import { toast } from 'react-toastify';
import { performLogout } from "../config/logout";
import {  registerShowModal } from "../utils/showRefreshTokenModal";

const apiUrl = import.meta.env.VITE_BACKEND_URL;

const RefreshTokenConfirmModal = () => {
  const [show, setShow] = useState(false);
  const [loading, setLoading] = useState(false);

  /**
  * Register a global function so this modal can be opened
  * from anywhere in the application (e.g., inside Axios interceptor).
  *
  * registerShowModal() stores a function internally.
  * Later, calling showRefreshTokenModal() triggers setShow(true).
  */
  useEffect(() => {
    registerShowModal(() => setShow(true));
  }, []);

  // Close modal manually
  const handleClose = () => setShow(false);

  /**
  * User chooses to cancel and not refresh the session.
  * This forces logout and clears stored tokens.
  */
  const handleCancel = async () => {
    await performLogout();
    setShow(false);
  };

  /**
 * Refresh token logic:
 * 1. Retrieve refresh token from sessionStorage
 * 2. Call backend refresh endpoint
 * 3. If successful → save new tokens and reload page
 * 4. If failed → logout and show error message
 */
  const handleConfirm = async () => {
    setLoading(true);
    try {
      // Get refresh token
      const refreshToken = sessionStorage.getItem("refresh_token"); // use sessionStorage consistently
      if (!refreshToken) {
        alert("No refresh token found. Please log in again.");
        await performLogout();
        return;
      }
      // Call backend to generate new access & refresh tokens
      const response = await axios.post(
        `${apiUrl}/api/v1/users/refresh-token/${refreshToken}`
      );

      if (response.data.status === "success") {
        // Save new tokens for session continuation
        sessionStorage.setItem("access_token", response.data.data.accessToken);
        sessionStorage.setItem("refresh_token", response.data.data.refreshToken);
        window.location.reload();
      } else {
        toast.error(response.data.message || "Failed to refresh session");
        setTimeout(async () => {
          await performLogout();
          window.location.reload();
        }, 1500);
      }
    } catch (error) {
      console.error("Refresh token error:", error);
      alert(error.response?.data?.message || "Something went wrong");
      await performLogout();
    } finally {
      setLoading(false);
      setShow(false);
    }
  };

  return (
    <Modal show={show} onHide={handleClose} centered>
      <Modal.Header closeButton>
        <Modal.Title className="text-center w-100">Session Expired</Modal.Title>
      </Modal.Header>

      <Modal.Body>
        <p>
          Your session has expired. Please log in again or confirm to refresh
          your session.
        </p>
      </Modal.Body>

      <Modal.Footer>
        <Button variant="danger" onClick={handleConfirm} disabled={loading}>
          {loading ? "Refreshing..." : "Confirm"}
        </Button>
        <Button variant="secondary" onClick={handleCancel} disabled={loading}>
          Cancel
        </Button>
      </Modal.Footer>
    </Modal>
  );
};


export default RefreshTokenConfirmModal;