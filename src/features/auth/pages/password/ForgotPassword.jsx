import { Container, Card, Form, Button, Spinner } from "react-bootstrap";
import styles from "./ForgotPassword.module.css";
import { useState, useEffect } from "react";
import { useNavigate, useParams, NavLink } from "react-router-dom"; // Added NavLink
import axios from "axios";
import { toast } from "react-toastify";
import { useDispatch, useSelector } from "react-redux";
import { fetchUserById } from "../../../../redux/slices/userSlice";

const apiUrl = import.meta.env.VITE_BACKEND_URL;

export default function ForgotPassword() {
  const [forgotPasswordData, setForgotPasswordData] = useState({ email: "" });
  const [formErrors, setFormErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const { id } = useParams();
  const dispatch = useDispatch();
  const { userDetails } = useSelector((state) => state.user);

  // Fetch user details if ID is present
  useEffect(() => {
    if (id) {
      dispatch(fetchUserById(id));
    }
  }, [dispatch, id]);

  // Pre-fill email if userDetails available
  useEffect(() => {
    if (userDetails?.emailId) {
      setForgotPasswordData({ email: userDetails.emailId });
    }
  }, [userDetails]);

  // Handle form input changes
  const handleChange = (event) => {
    const { name, value } = event.target;
    setForgotPasswordData({ ...forgotPasswordData, [name]: value });
    setFormErrors({ ...formErrors, [name]: "" });
  };

  // Validate email
  const validate = () => {
    const errors = {};
    if (
      !forgotPasswordData.email ||
      !/^[a-z0-9._%+:-]+@[a-z0-9.-]+\.[a-z]{2,}$/.test(forgotPasswordData.email)
    ) {
      errors.email = "Enter a valid email address";
    }
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Submit handler
  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!validate()) return;

    setLoading(true);
    try {
      const response = await axios.post(
        `${apiUrl}/api/v1/users/forgot-password-mail-send`,
        { email: forgotPasswordData.email },
        { headers: { "Content-Type": "application/json" } }
      );
      console.log("response------->", response);
      if (response.data.status === "success" || response.data.status === "Success" || response.data.status === "SUCCESS") {
        toast.success("Password reset email sent successfully.");
        setTimeout(() => navigate("/login"), 1500);
      } else {
        toast.error(`Failed: ${response.data.message}`);
      }
    } catch (error) {
      toast.error(
        `Failed: ${error.response?.data?.message || error.message}`
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container className={styles.container}>
      <Card className={styles.card}>
        <div className={styles.heading}>Forgot Password</div>
        <Form onSubmit={handleSubmit} noValidate>
          <Form.Group className="mb-3">
            <Form.Label htmlFor="email">Email</Form.Label>
            <Form.Control
              type="email"
              id="email"
              name="email"
              value={forgotPasswordData.email || ""}
              onChange={handleChange}
              isInvalid={!!formErrors.email}
              required
            />
            <Form.Control.Feedback type="invalid">
              {formErrors.email}
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
                  Sending...
                </>
              ) : (
                "Submit"
              )}
            </Button>
          </div>

          <div
            className={`d-flex justify-content-between mt-4 ${styles.urlDiv}`}
          >
            <NavLink to="/login" className={styles.link}>
              Login
            </NavLink>
            <NavLink to="/signup" className={styles.link}>
              Sign up
            </NavLink>
            
          </div>
        </Form>
      </Card>
    </Container>
  );
}
