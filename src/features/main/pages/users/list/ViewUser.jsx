import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useParams } from "react-router-dom";
import { ErrorMessage } from "../../../../../components/pages/ErrorMessage";
import { fetchUserById } from "../../../../../redux/slices/userSlice";

export function ViewUser() {

  const { id } = useParams();
  const dispatch = useDispatch();

  const { userDetails, loading, error } = useSelector((state) => state.user);

  // Fetch user details when component loads or when `id` changes
  useEffect(() => {
    if (id) {
      dispatch(fetchUserById(id));
    }
  }, [dispatch, id]);

  // Normalize and return a readable error message
  const getErrorMessage = (errorObj) => {
    if (!errorObj) return null;
    // If error is an object containing a message field
    if (typeof errorObj === "string") return errorObj;
    if (typeof errorObj === "object" && errorObj.message)
      return errorObj.message;
    return "An unknown error occurred";
  };
  if (error) {
    return <ErrorMessage message={getErrorMessage(error)} />;
  }

  return (
    <div>
      <div className="container my-4">
        {loading && <p>Loading user details...</p>}
        {error && (
          <p style={{ color: "red", fontWeight: "bold" }}>
            {getErrorMessage(error)}
          </p>
        )}
        {userDetails && (
          <div className="card shadow-sm border-0 rounded-3">
            <div className="card-header bg-dark text-white d-flex justify-content-between align-items-center">
              <h5 className="mb-0"> User Details</h5>
              <span
                className={`badge ${userDetails.active ? "bg-success" : "bg-secondary"}`}
              >
                {userDetails.active ? "Active" : "Inactive"}
              </span>
            </div>
            <div className="card-body">
              <div className="row mb-3">
                <div className="col-md-6">
                  <strong>First Name:</strong>
                  <p className="mb-0">{userDetails.firstName}</p>
                </div>
                <div className="col-md-6">
                  <strong>Last Name:</strong>
                  <p className="mb-0">{userDetails.lastName}</p>
                </div>
              </div>
              <div className="row mb-3">
                <div className="col-md-6">
                  <strong>Email:</strong>
                  <p className="mb-0">{userDetails.email}</p>
                </div>
                <div className="col-md-6">
                  <strong>User Name:</strong>
                  <p className="mb-0">{userDetails.userName}</p>
                </div>

              </div>
              <div className="row mb-3">
                <div className="col-md-6">
                  <strong>Date of Birth:</strong>
                  <p className="mb-0">{userDetails.dateOfBirth}</p>
                </div>
                <div className="col-md-6">
                  <strong>Mobile Number:</strong>
                  <p className="mb-0">{userDetails.phoneNumber}</p>
                </div>
              </div>
              <div className="row mb-3">
                <div className="col-md-6">
                  <strong>Country:</strong>
                  <p className="mb-0">{userDetails.country}</p>
                </div>
                <div className="col-md-6">
                  <strong>State:</strong>
                  <p className="mb-0">{userDetails.state}</p>
                </div>
              </div>
              <div className="row mb-3">
                <div className="col-md-6">
                  <strong>City:</strong>
                  <p className="mb-0">{userDetails.city}</p>
                </div>
                <div className="col-md-6">
                  <strong>Postal Code:</strong>
                  <p className="mb-0">{userDetails.postalCode}</p>
                </div>
              </div>
              <div className="row mb-3">
                <div className="col-md-6">
                  <strong>Address:</strong>
                  <p className="mb-0">{userDetails.address}</p>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
export default ViewUser;