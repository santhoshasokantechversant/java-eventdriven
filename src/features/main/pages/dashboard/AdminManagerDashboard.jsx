import axios from "axios";
import { useEffect, useState } from "react";
import PieChartComponent from "../../../../components/PieChartComponent";
import "./Dashboard.css";
import { showRefreshTokenModal } from "../../../../utils/showRefreshTokenModal";

const apiUrl = import.meta.env.VITE_BACKEND_URL;

const AdminManagerDashboard = () => {
    const [statusData, setStatusData] = useState([]);
    const [dashboard, setDashboard] = useState(null);

    useEffect(() => {
        // Fetch dashboard data when component mounts
        const fetchDashboard = async () => {
            try {
                const token = sessionStorage.getItem("access_token");
                if (!token) throw new Error("No access token found");
                // API call to fetch admin dashboard data
                const response = await axios.get(`${apiUrl}/api/v1/dashboard/admin`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json"
                    }
                });

                if (response.data.status === "success") {
                    const d = response.data.data;
                    setDashboard(d);

                    // Extract account statistics for pie chart
                    const active = d.activeAccounts;
                    const closed = d.totalClosedAccounts;

                    // If both are 0, provide small placeholder values
                    if (active === 0 && closed === 0) {
                        // Show placeholder slice for "No Accounts Found"
                        setStatusData([{ name: "No Accounts Found", value: 1 }]);
                    } else {
                        setStatusData([
                            { name: "Active Accounts", value: active },
                            { name: "Closed Accounts", value: closed },
                        ]);
                    }
                    // Handle Token Refresh scenario
                } else if (response.data.status === "error" && response.data.message.toLowerCase() === "network error") {
                    showRefreshTokenModal();
                }
                // Handle other API errors
                else {
                    if (response.data.status === "error" && response.data.message.toLowerCase() === "network error") {
                        showRefreshTokenModal();
                    }
                    console.error("Failed to fetch dashboard:", response.data.message);
                }
            } catch (error) {
                // Handle network/API errors
                if (error?.message?.toLowerCase() === "network error"
                ) {
                    showRefreshTokenModal();
                }
                console.error("Error fetching dashboard:", error);
            }
        };
        fetchDashboard();
    }, []);

    return (
        <div className="container-fluid charts-container">
            <h5 className="fw-bold">Dashboard</h5>
            <div className="row">
                {/* Left side cards */}
                <div className="col-md-7 d-flex">
                    <div className="m-auto">
                        <div className="row g-4">
                            <div className="col-md-6">
                                <div className="card text-center h-100 shadow-md">
                                    <div className="card-body">
                                        <h5 className="card-title">Total Customers</h5>
                                        <h3 className="card-text">{dashboard?.customerCount}</h3>
                                    </div>
                                </div>
                            </div>
                            <div className="col-md-6">
                                <div className="card text-center h-100 shadow-md">
                                    <div className="card-body">
                                        <h5 className="card-title">Total Accounts</h5>
                                        <h3 className="card-text">{dashboard?.totalAccounts}</h3>
                                    </div>
                                </div>
                            </div>
                            <div className="col-md-6">
                                <div className="card text-center h-100 shadow-md">
                                    <div className="card-body">
                                        <h5 className="card-title">Total Balance</h5>
                                        <h3 className="card-text">&#8377; {dashboard?.totalAmount}</h3>
                                    </div>
                                </div>
                            </div>
                            <div className="col-md-6">
                                <div className="card text-center h-100 shadow-md">
                                    <div className="card-body">
                                        <h5 className="card-title">Closed Accounts</h5>
                                        <h3 className="card-text">{dashboard?.totalClosedAccounts}</h3>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Active vs Inactive Accounts chart */}
                <div className="col-md-5 d-flex align-items-center justify-content-center mb-3">
                    {statusData.length > 0 ? (
                        <PieChartComponent data={statusData} dataKey="value" nameKey="name" />
                    ) : (
                        <p>Loading chart...</p>
                    )}
                </div>

                {/* Future charts */}
                {/* 
                <div className="row">
                    <div className="col-md-5 mt-3">
                        {accountTypeData.length > 0 ? (
                            <PieChartComponent data={accountTypeData} dataKey="value" nameKey="name" />
                        ) : (
                            <p>Account type chart coming soon...</p>
                        )}
                    </div>

                    <div className="col-md-5 ms-5 mt-3 mb-3">
                        {currencyData.length > 0 ? (
                            <BarChartComponent data={currencyData} dataKey="value" nameKey="name" />
                        ) : (
                            <p>Currency chart coming soon...</p>
                        )}
                    </div>
                </div> 
                */}
            </div>
        </div>
    );
};

export default AdminManagerDashboard;
