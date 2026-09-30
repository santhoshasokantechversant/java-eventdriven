import { Navigate } from "react-router-dom";
import PropTypes from 'prop-types';

const ProtectedRoute = ({ children }) => {
  const token = sessionStorage.getItem("access_token");

  if (!token) {
    // Not logged in → redirect to login
    return <Navigate to="/user-login" replace />;
  }

  // If logged in → render the protected component
  return children;
};

// Add prop types validation
ProtectedRoute.propTypes = {
  children: PropTypes.node.isRequired,
}

export default ProtectedRoute;
