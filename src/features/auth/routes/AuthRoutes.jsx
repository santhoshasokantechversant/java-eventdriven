import UserLogin from "../pages/login/UserLogin";
import UserSignup from "../pages/register/UserSignup";
import ForgotPassword from "../../auth/pages/password/ForgotPassword.jsx";
import SetPassword from "../../auth/pages/password/SetPassword.jsx";

// Authentication Routes Configuration
// These routes handle all authentication-related pages such as login, signup,
// forgot password, and setting a new password.
//
// Each object in the array represents a single route with:
//   - path: URL path for the route
const authRoutes = [
  { path: "*", element: <UserLogin /> },
  { path: "/signup", element: <UserSignup /> },
  {path: "/forgot-password", element: <ForgotPassword />},
  {path: "/set-password/:id", element: <SetPassword /> }
];

export default authRoutes;