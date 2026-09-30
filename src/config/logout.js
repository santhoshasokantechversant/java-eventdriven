import { toast } from "react-toastify";
import { logoutUser } from "../redux/slices/authSlice";
import store from "../redux/store";
/**
 * Performs the logout operation.
 *
 * This function handles:
 * 1. Dispatching the Redux logout action.
 * 2. Redirecting the user to the login page.
 * 3. Showing an error notification if logout fails.
 */
export const performLogout = async () => {
  try {
    // Dispatch the logout action to clear authentication state from Redux
    await store.dispatch(logoutUser());
    // Redirect to login page after successful logout
    window.location.href = "/user-login";
  } catch (err) {
    // Redirect even if logout fails
    window.location.href = "/user-login";
    toast.error(`Logout error: ${err}`);
  }
};
