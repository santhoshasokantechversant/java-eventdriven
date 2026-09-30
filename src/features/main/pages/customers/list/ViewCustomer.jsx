import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useParams } from "react-router-dom";
import { getAccountByCustomerId } from "../../../../../redux/slices/accountSlice";
import { fetchCustomerById } from "../../../../../redux/slices/customerSlice";

export const ViewCustomer = () => {
  const { id } = useParams();
  const dispatch = useDispatch();

  // Get selected customer details from Redux store
  const { selectedCustomer: customer, loading, error } = useSelector(
    (state) => state.customers
  );

  // Get customer's account details from Redux store
  const { currentAccount } = useSelector(
    (state) => state.accounts
  );

  // Fetch customer details when page loads or ID changes
  useEffect(() => {
    if (id) {
      dispatch(fetchCustomerById(id));
    }
  }, [dispatch, id]);

  // Fetch account details after customer data loads
  useEffect(() => {
    if (customer?.customerId) {
      dispatch(getAccountByCustomerId(customer.customerId));
    }
  }, [dispatch, customer]);

  // Loading state
  if (loading) {
    return (
      <div className="container my-4">
        <p>Loading customer details...</p>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="container my-4">
        <p className="text-danger">Error: {error}</p>
      </div>
    );
  }

  // No customer found
  if (!customer) {
    return (
      <div className="container my-4">
        <p>No customer found.</p>
      </div>
    );
  }

  return (
    <div className="container my-2">
      <div className="card shadow-sm border-0 rounded-3">
        <div className="card-header bg-dark text-white d-flex justify-content-between align-items-center">
          <h5 className="mb-0">Customer Details</h5>
          <span
            className={`badge ${customer.status === "ACTIVE" ? "bg-success" : "bg-danger"
              }`}
          >
            {customer.status}
          </span>
        </div>

        {/* customer details */}
        <div className="card-body">
          <div className="row mb-3">
            <div className="col-md-6">
              <strong>First Name:</strong>
              <p className="mb-0">{customer.firstName}</p>
            </div>
            <div className="col-md-6">
              <strong>Last Name:</strong>
              <p className="mb-0">{customer.lastName}</p>
            </div>
          </div>
          <div className="row mb-3">
            <div className="col-md-6">
              <strong>Email:</strong>
              <p className="mb-0">{customer.email}</p>
            </div>
            <div className="col-md-6">
              <strong>Mobile Number:</strong>
              <p className="mb-0">{customer.phoneNumber}</p>
            </div>
          </div>
          <div className="row mb-3">
            <div className="col-md-6">
              <strong>Date of Birth:</strong>
              <p className="mb-0">{customer.dateOfBirth}</p>
            </div>
            <div className="col-md-6">
              <strong>Country:</strong>
              <p className="mb-0">{customer.country}</p>
            </div>
          </div>
          <div className="row mb-3">
            <div className="col-md-6">
              <strong>State:</strong>
              <p className="mb-0">{customer.state}</p>
            </div>
            <div className="col-md-6">
              <strong>City:</strong>
              <p className="mb-0">{customer.city}</p>
            </div>
          </div>
          <div className="row mb-3">
            <div className="col-md-6">
              <strong>Postal Code:</strong>
              <p className="mb-0">{customer.postalCode}</p>
            </div>
            <div className="col-md-6">
              <strong>Address:</strong>
              <p className="mb-0">{customer.address}</p>
            </div>
          </div>
        </div>

        {/* account details  */}
        {currentAccount && (
          <>
            <div className="card-header bg-dark text-white d-flex justify-content-between align-items-center">
              <h5 className="mb-0">Account Details</h5>
            </div>
            <div className="card-body">
              <div className="row mb-3">
                <div className="col-md-6">
                  <strong>Account Number:</strong>
                  <p className="mb-0">{currentAccount.accountNumber}</p>
                </div>
                <div className="col-md-6">
                  <strong>Account Type:</strong>
                  <p className="mb-0">{currentAccount.accountType}</p>
                </div>
              </div>
              <div className="row mb-3">
                <div className="col-md-6">
                  <strong>Account Status:</strong>
                  <p className="mb-0">{currentAccount.status}</p>
                </div>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default ViewCustomer;
