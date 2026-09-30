import { Container } from "react-bootstrap";
import styles from "../styles/Message.module.css";
import PropTypes from "prop-types";

/**
 * Message Component
 * ------------------
 * Displays a brief user info section including:
 * - User name
 * - Email
 * - Last login time
 * - User profile icon (Bootstrap icon)
 *
 * This component is typically used in dashboards or header sections
 * to show the currently logged-in user's basic details.
 */
export function Message({ userName, email, lastLogin }) {
  return (
    <Container fluid>
      <div className={styles.mainDiv}>
        <div className={styles.nameDiv}>Hello, {userName} !</div>
        <div>|</div>
        <div className={styles.emailDiv}>{email}</div>
        <div>|</div>
        <div className={styles.lastLoginDiv}>{lastLogin}</div>
        <div>|</div>
        <div>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="30"
            height="35"
            fill="currentColor"
            className="bi bi-person-circle"
            viewBox="0 0 20 20"
          >
            <path d="M11 6a3 3 0 1 1-6 0 3 3 0 0 1 6 0" />
            <path
              fillRule="evenodd"
              d="M0 8a8 8 0 1 1 16 0A8 8 0 0 1 0 8m8-7a7 7 0 0 0-5.468 11.37C3.242 11.226 4.805 10 8 10s4.757 1.225 5.468 2.37A7 7 0 0 0 8 1"
            />
          </svg>
        </div>
      </div>
    </Container>
  );
};

// Add prop type validation 
Message.propTypes = {
  userName: PropTypes.string.isRequired,
  email: PropTypes.string.isRequired,
  lastLogin: PropTypes.string.isRequired,
};
