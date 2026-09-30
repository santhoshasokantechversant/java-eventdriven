import { unwrapResult } from "@reduxjs/toolkit";
import { useState } from "react";
import { Button, Card, Col, Container, Form, Row } from "react-bootstrap";
import { useDispatch, useSelector } from "react-redux";
import { NavLink, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { loginUser } from "../../../../redux/slices/authSlice";
import styles from "./UserLogin.module.css";

export default function UserLogin() {

  const [loginData, setLoginData] = useState({ email: "", passwordHash: "" });
  const [formErrors, setFormErrors] = useState({});
  const dispatch = useDispatch();
  // Redux state for login request status
  const { loading, error} = useSelector(
    (state) => state.auth
  );
  const navigate = useNavigate();
  const [user, setUser] = useState({});
  /**
   * Handle form input changes
   * Updates loginData and clears error of that specific field
   */
  const handleChange = (event) => {
    const { name, value } = event.target;
    setLoginData((prev) => ({ ...prev, [name]: value }));

    // Clear error for this field
    setFormErrors((prev) => ({ ...prev, [name]: "" }));
  };

  // Validation function
  const validate = () => {
    const errors = {};
    if (!loginData.email.trim()) {
      errors.email = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(loginData.email)) {
      errors.email = "Enter a valid email address";
    }

    if (!loginData.passwordHash.trim()) {
      errors.passwordHash = "Password is required";
    } else if (loginData.passwordHash.length < 8) {
      errors.passwordHash = "Password must be at least 8 characters";
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

    /**
   * Handle form submit
   * - Validates input
   * - Dispatches login API request
   * - Saves tokens & user data in sessionStorage
   * - Navigates user to their allowed first route
   */
  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!validate()) return; // stop if validation fails

    try {
      const resultAction = await dispatch(loginUser(loginData));
      const userData = unwrapResult(resultAction);

      sessionStorage.setItem("access_token", userData.data.token.accessToken);
      sessionStorage.setItem("refresh_token", userData.data.token.refreshToken);
      sessionStorage.setItem("fullName", userData.data.fullName);
      sessionStorage.setItem("roleId", userData.data.roleId);
      sessionStorage.setItem("userId", userData.data.id);
      sessionStorage.setItem("sideNav", JSON.stringify(userData.data.sidenav));

      setUser({
        userName: userData.data.userName,
        email: userData.data.emailId,
      });
      if (userData?.data) {
        if (userData.data.sidenav?.length > 0) {
          navigate(userData.data.sidenav[0].slugName, { replace: true, state: user })
        } else {
          navigate("/under-development", { replace: true, state: user });
        }
      }
    } catch (err) {
      console.error("Login failed:", err);
      toast.error(`Login failed: ${error.message}`);
    }
  };

  // useEffect(() => {
  //   if (isAuthenticated) {
  //     navigate("/admin-manager-dashboard", { replace: true, state: user });
  //   }
  // }, [isAuthenticated, navigate, user]);

  return (
    <Container
      fluid
      className="d-flex justify-content-center align-items-center"
    >
      <Row className="w-100 justify-content-center">
        <Col xs={12} sm={10} md={8} lg={5} xl={4}>
          <Card className={`p-2  ${styles.loginBody}`}>
            <Card.Body>
              <h3 className={`text-center mb-4 ${styles.heading}`}>Login</h3>
              <Form onSubmit={handleSubmit} noValidate>
                <Form.Group className="mb-3" controlId="formEmail">
                  <Form.Control
                    type="email"
                    name="email"
                    placeholder="Username"
                    value={loginData.email}
                    onChange={handleChange}
                    isInvalid={!!formErrors.email}
                  />
                  <Form.Control.Feedback type="invalid">
                    {formErrors.email}
                  </Form.Control.Feedback>
                </Form.Group>

                <Form.Group className="mb-3" controlId="formPassword">
                  <Form.Control
                    type="password"
                    name="passwordHash"
                    placeholder="Password"
                    value={loginData.passwordHash}
                    onChange={handleChange}
                    isInvalid={!!formErrors.passwordHash}
                  />
                  <Form.Control.Feedback type="invalid">
                    {formErrors.passwordHash}
                  </Form.Control.Feedback>
                </Form.Group>

                <div className="d-flex justify-content-center">
                  <Button
                    variant="primary"
                    className={`w-50 mt-3`}
                    type="submit"
                    disabled={loading}
                  >
                    {loading ? "Logging in..." : "Login"}
                  </Button>
                </div>
              </Form>

              <div
                className={`d-flex justify-content-between mt-4 ${styles.urlDiv}`}
              >
                <NavLink to="/signup" className={styles.link}>
                  Sign up
                </NavLink>
                <NavLink to="/forgot-password" className={styles.link}>
                  Forgot password?
                </NavLink>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
}
