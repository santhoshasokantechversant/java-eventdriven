import React, { useEffect, useState } from "react";
import "./Dashboard.css";
import axios from "axios";
import { showRefreshTokenModal } from "../../../../utils/showRefreshTokenModal";
const apiUrl = import.meta.env.VITE_BACKEND_URL;

export const CustomerDashboard = () => {
  const [account, setAccount] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fetch logged-in customer's account details
    const fetchAccount = async () => {
      try {
        const id = sessionStorage.getItem("userId");
        const token = sessionStorage.getItem("access_token");
        // Validate required session values
        if (!id) throw new Error("No account ID found in session");
        // API request to fetch customer dashboard data
        const response = await axios.get(
          `${apiUrl}/api/v1/dashboard/customer/${id}`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
        // Success response handling
        if (response.data.status === "success") {
          setAccount(response.data.data);
        }
        // Token expired / network error case
        else if (response.data.status === "error" && response.data.message.toLowerCase() === "network error") {
          showRefreshTokenModal();
        }
        // Other API errors
        else {
          if (response.data.status === "error" && response.data.message.toLowerCase() === "network error") {
            showRefreshTokenModal();
          }
          console.error("Failed to fetch account:", response.data.message);
        }
      } catch (error) {
        // Handle fetch errors such as invalid token / network issues
        if (error?.message?.toLowerCase() === "network error"
        ) {
          showRefreshTokenModal();
        }
        console.error("Error fetching account:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchAccount();
  }, []);

  // Show loader until API completes
  if (loading) {
    return (
      <div className="container-fluid">
        <h5 className="fw-bold mb-3">Loading Dashboard...</h5>
      </div>
    );
  }
  // Show loader until API completes
  if (!account) {
    return (
      <div className="container-fluid">
        <h5 className="fw-bold mb-3">No account data found</h5>
      </div>
    );
  }

  return (
    <div className="container-fluid">
      <h5 className="fw-bold mb-3">Customer Dashboard</h5>
      <div className="row g-3">
        <div className="col-12 col-md-4">
          {/* Account number card */}
          <div className="card text-center h-100 shadow-md">
            <div className="card-body py-3">
              <h5 className="card-title">Account Number</h5>
              <h5 className="card-text">{account.accountNumber}</h5>
            </div>
          </div>
        </div>
        {/* Account balance card */}
        <div className="col-12 col-md-4">
          <div className="card text-center h-100 shadow-md">
            <div className="card-body py-3">
              <h5 className="card-title">Account Balance</h5>
              <h5 className="card-text">
                ₹ {Number(account.balance).toLocaleString("en-IN", { minimumFractionDigits: 2 })}
              </h5>
            </div>
          </div>
        </div>
        {/* Currency card */}
        <div className="col-12 col-md-4">
          <div className="card text-center h-100 shadow-md">
            <div className="card-body py-3">
              <h5 className="card-title">Currency</h5>
              <h5 className="card-text">
                {account.currency?.currencyCode || "N/A"}
              </h5>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
