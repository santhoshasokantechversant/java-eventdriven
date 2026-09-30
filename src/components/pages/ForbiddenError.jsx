import { Container } from "react-bootstrap";
import styles from "../../styles/ForbiddenError.module.css";

/**
 * ForbiddenError Component
 * ------------------------
 * Displays a 403 Forbidden error message.
 * This component is typically shown when the user tries to access a page
 * or resource for which they do not have permissions.
 *
 * Structure:
 * - Main heading (403 Forbidden)
 * - Subheading explaining the issue
 * - Button to redirect user (e.g., dashboard)
 */
export function ForbiddenError() {
  return (
    <Container className={styles.content}>
      <div className={styles.mainHeading}>403 Forbidden</div>
      <div className={styles.subHeading}>
        You donot have permission to access this page.
      </div>
      <div className={styles.buttonDiv}>
        <button 
        type="button"
        className={styles.button}>Go to Dashboard</button>
      </div>
    </Container>
  );
}
export default ForbiddenError;