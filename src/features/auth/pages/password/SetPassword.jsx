import { Container, Card, Form, Button, Spinner, Alert } from "react-bootstrap";
import styles from "./ForgotPassword.module.css";
import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";

const apiUrl = import.meta.env.VITE_BACKEND_URL;

export default function SetPassword() {
   // Local state to store user details and form inputs
  const [userData, setUserData] = useState({
    email: "",
    userName: "",
    password: "",
    confirmPassword: "",
  });
  const [formErrors, setFormErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null); // Stores GET/POST messages
  const [messageType, setMessageType] = useState(null); // "success" or "error"
  const navigate = useNavigate();
  const { id } = useParams();
  const [formVisible, setFormVisible] = useState(true); // Control form visibility

  /**
   * Fetch user details using encrypted ID
   * → Pre-fill email + username
   * → If error, hide form and display error message
   */
  useEffect(() => {
    const fetchUser = async () => {
      try {
        const res = await axios.get(`${apiUrl}/api/v1/users/get-by-id-encrypted/${id}`);
        if (res.data.status === "success") {
          setUserData(prev => ({
            ...prev,
            email: res.data.data.email,
            userName: res.data.data.userName,
          }));
        } else {
          setMessage(res.data.message);
          setMessageType("error");
          setFormVisible(false); // hide form on error
        }
      } catch (error) {
        setMessage(error.response?.data?.message || error.message);
        setMessageType("error");
        setFormVisible(false); // hide form on error
      }
    };
    fetchUser();
  }, [id]);

    /**
   * Update form inputs and clear field-specific error
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setUserData(prev => ({ ...prev, [name]: value }));
    setFormErrors(prev => ({ ...prev, [name]: "" }));
  };

    /**
   * Validate password fields
   * → Minimum length 8
   * → Passwords must match
   */
  const validate = () => {
    const errors = {};
    if (!userData.password || userData.password.length < 8)
      errors.password = "Password must be at least 8 characters long";
    if (!userData.confirmPassword || userData.confirmPassword.length < 8)
      errors.confirmPassword = "Password must be at least 8 characters long";
    if (userData.password !== userData.confirmPassword)
      errors.confirmPassword = "Passwords do not match";
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

    /**
   * Submit handler for setting new password
   * → Validates form
   * → Sends POST request to API
   * → Shows success/error messages
   * → Redirects to login page on success
   */
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      const res = await axios.post(`${apiUrl}/api/v1/users/reset-password`, {
        encryptedUserId: id,
        email: userData.email,
        password: userData.password,
        confirmPassword: userData.confirmPassword,
      });

      setMessage(res.data.message);
      setMessageType(res.data.status === "success" ? "success" : "error");
      setFormVisible(false); // hide form after submission

      if (res.data.status === "success") {
        setTimeout(() => navigate("/login"), 2000);
      }
    } catch (error) {
      setMessage(error.response?.data?.message || error.message);
      setMessageType("error");
      setFormVisible(false); // hide form on error
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container className="d-flex justify-content-center align-items-center" style={{ minHeight: "100vh" }}>
      <Card className={styles.forgotPasswordBody} style={{ width: "400px", padding: "20px" }}>
        {message && (
          <Alert variant={messageType === "success" ? "success" : "danger"} className="text-center mb-3">
            {messageType === "success" ? "✔ " : "✖ "} {message}
          </Alert>
        )}

        {formVisible && (
          <>
            <div className={styles.heading}>Set Password</div>
            <Form onSubmit={handleSubmit} noValidate>
              <Form.Group className="mb-2">
                <Form.Label>Email</Form.Label>
                <Form.Control
                  type="email"
                  name="email"
                  value={userData.email}
                  readOnly
                />
              </Form.Group>

              <Form.Group className="mb-2">
                <Form.Label>Username</Form.Label>
                <Form.Control
                  type="text"
                  name="userName"
                  value={userData.userName}
                  readOnly
                />
              </Form.Group>

              <Form.Group className="mb-2">
                <Form.Label>Password</Form.Label>
                <Form.Control
                  type="password"
                  name="password"
                  value={userData.password}
                  onChange={handleChange}
                  isInvalid={!!formErrors.password}
                  required
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.password}
                </Form.Control.Feedback>
              </Form.Group>

              <Form.Group className="mb-3">
                <Form.Label>Confirm Password</Form.Label>
                <Form.Control
                  type="password"
                  name="confirmPassword"
                  value={userData.confirmPassword}
                  onChange={handleChange}
                  isInvalid={!!formErrors.confirmPassword}
                  required
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.confirmPassword}
                </Form.Control.Feedback>
              </Form.Group>

              <div className="d-grid">
                <Button type="submit" variant="primary" disabled={loading}>
                  {loading ? (
                    <>
                      <Spinner
                        as="span"
                        animation="border"
                        size="sm"
                        role="status"
                        aria-hidden="true"
                      />{" "}
                      Changing password...
                    </>
                  ) : (
                    "Submit"
                  )}
                </Button>
              </div>
            </Form>
          </>
        )}
      </Card>
    </Container>
  );
}