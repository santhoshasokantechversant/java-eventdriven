import { Container, Card, Form, Button, Spinner } from "react-bootstrap";
import styles from "./UserSignup.module.css";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";

const apiUrl = import.meta.env.VITE_BACKEND_URL;

export default function UserSignup() {
  const [signupData, setSignupData] = useState({});
  const [loading, setLoading] = useState(false);
  const [formErrors, setFormErrors] = useState({});
  const navigate = useNavigate();

    /**
   * Handle input field changes
   * - Update signupData object
   * - Clear errors for modified field
   */
  const handleChange = (event) => {
    const { name, value } = event.target;
    const updatedData = { ...signupData, [name]: value };
    setSignupData(updatedData);

    // Clear field error
    setFormErrors((prev) => ({ ...prev, [name]: "" }));
  };

    /**
   * Validate form fields
   * - Checks email format
   * - Password minimum length
   * - Confirm password matching
   */
  const validate = () => {
    const errors = {};
    if (
      !signupData.email ||
       !/^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$/.test(signupData.email)
    ) {
      errors.email = "Enter a valid email address";
    }

    if (!signupData.password || signupData.password.length < 8) {
      errors.password = "Password must be at least 8 characters long";
    }

    if (!signupData.confirmPassword || signupData.confirmPassword.length < 8) {
      errors.confirmPassword = "Password must be at least 8 characters long";
    }

    if (signupData.password !== signupData.confirmPassword) {
      errors.confirmPassword = "Passwords do not match";
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

   /**
   * Submit signup form
   * - Performs validation
   * - Sends API request to create new user
   * - Shows toast messages based on response
   * - Redirects to login page on success
   */
  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!validate()) return;

    setLoading(true);
    try {
      const response = await axios.post(`${apiUrl}/api/v1/users/register-new`, {
        emailId: signupData.email,
        password: signupData.password,
        confirmPassword: signupData.confirmPassword,
      });

      if (response.data.status === "success") {
        toast.success("Registration completed successfully.");
        setTimeout(() => navigate("/login"), 1500);
      } else {
        toast.error(`Registration failed: ${response.data.message}`);
      }
    } catch (error) {
      toast.error(
        `Registration failed: ${error.response?.data?.message || error.message}`
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container>
      <Card className={styles.signupBody}>
        <div className={styles.heading}>User Registration</div>
        <Form className={styles.formBody} onSubmit={handleSubmit} noValidate>
          <Form.Group className="mb-2">
            <Form.Label htmlFor="email">Email</Form.Label>
            <Form.Control
              type="email"
              id="email"
              name="email"
              value={signupData.email || ""}
              onChange={handleChange}
              isInvalid={!!formErrors.email}
              required
            />
            <Form.Control.Feedback type="invalid" className={styles.errorFeedback}>
              {formErrors.email}
            </Form.Control.Feedback>
          </Form.Group>

          <Form.Group className="mb-2">
            <Form.Label htmlFor="password">Password</Form.Label>
            <Form.Control
              type="password"
              id="password"
              name="password"
              value={signupData.password || ""}
              onChange={handleChange}
              isInvalid={!!formErrors.password}
              required
            />
            <Form.Control.Feedback type="invalid" className={styles.errorFeedback}>
              {formErrors.password}
            </Form.Control.Feedback>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label htmlFor="confirmPassword">Re-type Password</Form.Label>
            <Form.Control
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={signupData.confirmPassword || ""}
              onChange={handleChange}
              isInvalid={!!formErrors.confirmPassword}
              required
            />
            <Form.Control.Feedback type="invalid"  className={styles.errorFeedback}>
              {formErrors.confirmPassword}
            </Form.Control.Feedback>
          </Form.Group>

          <div className={styles.submitButton}>
            <Button variant="primary" type="submit" disabled={loading}>
              {loading ? (
                <>
                  <Spinner
                    as="span"
                    animation="border"
                    size="sm"
                    role="status"
                    aria-hidden="true"
                  />{" "}
                  Creating...
                </>
              ) : (
                "Create"
              )}
            </Button>
          </div>

          {/* Login redirect link */}
          <div className="text-center mt-3">
            <span>Already have an account? </span>
            <Button
              variant="link"
              className="p-0 fw-semibold"
              onClick={() => navigate("/login")}
            >
              Login
            </Button>
          </div>

        </Form>
      </Card>
    </Container>
  );
}
