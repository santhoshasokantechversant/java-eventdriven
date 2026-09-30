import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { getAccountById } from '../../../../../redux/slices/accountSlice';
import { fetchCustomerById } from '../../../../../redux/slices/customerSlice';

export const ViewAccount = () => {
    const { id } = useParams();
    const dispatch = useDispatch();

    // Get current account details from Redux store
    const { currentAccount, loading: accountLoading, error: accountError } = useSelector(
        (state) => state.accounts
    );

    // Get selected customer details from Redux store
    const { selectedCustomer: customer, loading: customerLoading, error: customerError } = useSelector(
        (state) => state.customers
    );

    // Fetch account on mount
    useEffect(() => {
        if (id) {
            dispatch(getAccountById(id));
        }
    }, [dispatch, id]);

    // Fetch customer once account is loaded
    useEffect(() => {
        if (currentAccount?.customerId) {
            dispatch(fetchCustomerById(currentAccount.customerId));
        }
    }, [dispatch, currentAccount]);

    if (accountLoading || customerLoading) {
        return (
            <div className="container my-4">
                <p>Loading account details...</p>
            </div>
        );
    }

    if (accountError || customerError) {
        return (
            <div className="container my-4">
                <p className="text-danger">Error: {accountError || customerError}</p>
            </div>
        );
    }

    if (!currentAccount) {
        return (
            <div className="container my-4">
                <p>No accounts found.</p>
            </div>
        );
    }

    return (
        <div className="container my-4">
            <div className="card shadow-sm border-0 rounded-3">
                <div className="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <h5 className="mb-0">Account Details</h5>
                    <span
                        className={`badge ${currentAccount.status === "ACTIVE" ? "bg-success" : "bg-danger"
                            }`}
                    >
                        {currentAccount.status}
                    </span>
                </div>
                {/* account details  */}
                <div className="card-body">
                    <div className="row mb-3">
                        <div className="col-md-6">
                            <strong>Account Number:</strong>
                            <p className="mb-0">{currentAccount.accountNumber}</p>
                        </div>
                        <div className="col-md-6">
                            <strong>Balance:</strong>
                            <p className="mb-0">{currentAccount.balance}</p>
                        </div>
                    </div>
                    <div className="row mb-3">
                        <div className="col-md-6">
                            <strong>Account Type:</strong>
                            <p className="mb-0">{currentAccount.accountType}</p>
                        </div>
                        <div className="col-md-6">
                            <strong>Currency:</strong>
                            <p className="mb-0">{currentAccount.currency?.currencyCode}</p>
                        </div>
                    </div>
                </div>
                {/* customer details  */}
                {customer && (
                    <>
                        <div className="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                            <h5 className="mb-0">Customer Details</h5>
                        </div>
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
                                    <strong>Customer No:</strong>
                                    <p className="mb-0">{customer.customerNo}</p>
                                </div>
                                <div className="col-md-6">
                                    <strong>Email:</strong>
                                    <p className="mb-0">{customer.email}</p>
                                </div>
                            </div>
                            <div className="row mb-3">
                                <div className="col-md-6">
                                    <strong>Phone No:</strong>
                                    <p className="mb-0">{customer.phoneNumber}</p>
                                </div>
                            </div>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
};

export default ViewAccount;
