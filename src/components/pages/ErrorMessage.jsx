import { Container } from "react-bootstrap";
import styles from "../../styles/ErrorMessage.module.css";
import PropTypes from "prop-types";
import { useNavigate } from "react-router-dom";

/**
 * ErrorMessage Component
 * ----------------------
 * A reusable UI component to display error messages across the application.
 * It optionally shows a button that can redirect the user to a given route.
 *
 * Props:
 * - message (String): The error message to display.
 * - showButton (Boolean): Whether to show a navigation button.
 * - buttonText (String): Text to display inside the button.
 * - to (String): Path to navigate when button is clicked.
 */
export function ErrorMessage({
  message,
  showButton = false,
  buttonText = "Go Back",
  to = "/",
}) {
  const navigate = useNavigate();
  if (!message) return null;
  return (
    <Container className={styles.content}>
      <div className={styles.mainHeading}>Error</div>
      <div className={styles.subHeading}>{message}</div>
      {showButton && (
        <div className={styles.buttonDiv}>
          <button
           type="button"
          className={styles.button} onClick={() => navigate(to)}>
            {buttonText}
          </button>
        </div>
      )}
    </Container>
  );
}

ErrorMessage.propTypes = {
  message: PropTypes.string,
  showButton: PropTypes.bool,
  buttonText: PropTypes.string,
  to: PropTypes.string,
};
