import Container from "react-bootstrap/Container";
import { useEffect, useState } from 'react';
import { Button } from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import FilterUserModal from '../../../../../components/FilterUserModal';
import { ConfirmModal } from '../../../../../components/ConfirmModal';
import EditPageModal from '../../../../../components/EditPageModal';
import { ToastMessage } from '../../../../../components/ToastMessage';
import EditAccount from '../update/EditAccount.jsx';
import './AccountDashboard.css';
import {
    deleteAccountById,
    fetchAccounts,
    fetchAccountsStatus,
    fetchAccountTypes,
    fetchAllCurrency
} from '../../../../../redux/slices/accountSlice';
import AdvancedPagination from "../../../../../components/CustomPagination.jsx";

export function AccountDashboard() {
    const dispatch = useDispatch();
    const navigate = useNavigate();

    // Main filter used for fetching data
    const [filter, setFilter] = useState({
        accountNumber: null,
        accountType: null,
        currencyId: null,
        customerNo: null,
        status: null,
        sortField: "accNo",
        sortDirection: "DESC",
        page: 0,
        size: 10
    });

    // Local filter used inside modal
    const [localFilter, setLocalFilter] = useState({ ...filter });

    const { accounts, loading, error, accountTypes, currency, accountStatus } = useSelector((state) => state.accounts);

    // Modals
    const [showFilterModal, setShowFilterModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedAccount, setSelectedAccount] = useState(null);

    // Toast
    const [toast, setToast] = useState({ show: false, message: "", bg: "success" });

    // Pagination info
    const totalPages = accounts?.totalPages || 1;
    const currentPage = filter.page;
    const pageSize = accounts?.pageSize || 1;
    const totalItems = accounts?.totalItems || 1;

    // Filter modal fields
    const filterFields = [
        { name: "accountNumber", placeholder: "Search by Account No", type: "text" },
        { name: "accountType", placeholder: "Select Account Type", type: "select", options: accountTypes?.map(type => ({ id: type, name: type })) || [] },
        { name: "currencyId", placeholder: "Select Currency", type: "select", options: currency?.map(c => ({ id: c.id, name: c.currencyCode })) || [] },
        { name: "status", placeholder: "Select Status", type: "select", options: accountStatus?.map(s => ({ id: s, name: s })) || [] },
        { name: "customerNo", placeholder: "Search by Customer No", type: "text" },
    ];

    // Fetch dropdown data
    useEffect(() => {
        dispatch(fetchAccountTypes());
        dispatch(fetchAllCurrency());
        dispatch(fetchAccountsStatus());
    }, [dispatch]);

    // Fetch accounts whenever main filter changes
    useEffect(() => {
        dispatch(fetchAccounts(filter));
    }, [filter, dispatch]);

    // Show toast on backend error
    useEffect(() => {
        if (error) {
            setToast({ show: true, message: error, bg: "danger" });
        }
    }, [error]);

    // Delete account
    const deleteAccount = (id) => {
        dispatch(deleteAccountById(id))
            .unwrap()
            .then(() => {
                setShowDeleteModal(false);
                dispatch(fetchAccounts(filter));
            })
            .catch((err) => {
                setToast({ show: true, message: "Error deleting account: " + err, bg: "danger" });
            });
    };

    // Filter modal input change
    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setLocalFilter(prev => ({ ...prev, [name]: value || null }));
    };

    // Apply filter: update main filter and close modal
    const handleFilterSearch = () => {
        setFilter(prev => ({ ...prev, ...localFilter, page: 0 })); // reset to first page
        setShowFilterModal(false);
    };

    const handleOpenFilterModal = () => {
        setLocalFilter({ ...filter });
        setShowFilterModal(true);
    };
    const handleCloseFilterModal = () => setShowFilterModal(false);

    // Clear all filters
    const handleClear = () => {
        const defaultFilter = {
            accountNumber: null,
            accountType: null,
            currencyId: null,
            status: null,
            customerNo: null,
            sortField: "accNo",
            sortDirection: "DESC",
            page: 0,
            size: 10
        };
        setFilter(defaultFilter);
        setLocalFilter(defaultFilter);
        setShowFilterModal(false);
    };

    // Pagination change
    const handlePageChange = (page) => {
        setFilter(prev => ({ ...prev, page }));
    };

    // pagination page and size change
    const handlePageSizeChange = (e) => {
        const newSize = Number.parseInt(e.target.value);
        setFilter(prev => ({ ...prev, size: newSize, page: 0 }));
    };

    // Edit modal
    const handleOpenEditModal = (account) => {
        setSelectedAccount(account);
        setShowEditModal(true);
    };

    // function for close the edit modal
    const handleCloseEditModal = (success, message) => {
        setShowEditModal(false);
        setSelectedAccount(null);
        if (success === true) {
            setToast({ show: true, message, bg: "success" });
            dispatch(fetchAccounts(filter));
        } else if (success === false && message) {
            setToast({ show: true, message, bg: "danger" });
        }
    };


    const stickyHeaderStyle = {
        position: "sticky",
        top: 0,
        backgroundColor: "#212529",
        color: "white",
        zIndex: 10,
    };

    return (
        <>
            {/* css style for scrollable page */}
            <style>{`
                .scrollableContainer::-webkit-scrollbar {
                width: 12px;
                height: 12px;
                }

                .scrollableContainer::-webkit-scrollbar-thumb {
                background-color: #bebefc;
                border-radius: 6px;
                }

                .scrollableContainer::-webkit-scrollbar-track {
                background: #f1f1f1;
                }

                .scrollableContainer {
                scrollbar-width: thin;
                scrollbar-color: #bebefc #f1f1f1;
                }
            `}</style>
            <div className="container-fluid">
                {loading && <div className='spinner'></div>}
                {!loading &&
                    <>
                        <Container fluid>
                            <div className="row g-4 align-items-center mb-4">
                                <div className="topDivision">
                                    <div>
                                        <h5 className='mainContent'>Manage Accounts</h5>
                                    </div>
                                    <div className="buttonDiv">
                                        <Button variant="primary" onClick={handleOpenFilterModal}>
                                            Filter
                                        </Button>
                                        <Button variant="danger" onClick={handleClear}>
                                            Clear
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </Container>

                        <Container fluid style={{ maxHeight: "350px" }} className="scrollableContainer">
                            <div
                                style={{
                                    width: "80vw", // full viewport width
                                    overflowX: "auto", // enable horizontal scroll
                                    overflowY: "auto",
                                    maxHeight: "50vh", // optional: vertical limit
                                    whiteSpace: "nowrap", // prevent wrapping
                                    marginLeft: "auto",
                                    marginRight: "auto",
                                }}
                                className="scrollableContainer"
                            >
                                <table className="table table-bordered table-striped align-middle">
                                    <thead className="table-dark">
                                        <tr>
                                            <th style={stickyHeaderStyle}>Sl. No.</th>
                                            <th style={stickyHeaderStyle}>Account Number</th>
                                            <th style={stickyHeaderStyle}>Customer No.</th>
                                            <th style={stickyHeaderStyle}>Balance</th>
                                            <th style={stickyHeaderStyle}>Account Type</th>
                                            <th style={stickyHeaderStyle}>Currency</th>
                                            <th style={stickyHeaderStyle}>Status</th>
                                            <th style={stickyHeaderStyle}>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {accounts?.account?.length > 0 ? (
                                            accounts.account.map((items, index) => (
                                                <tr key={items.id}>
                                                    <td onClick={() => navigate(`/view-account/${items.id}`)}>{index + 1}</td>
                                                    <td>{items.accountNumber}</td>
                                                    <td>{items.customerNo}</td>
                                                    <td>{items.balance}</td>
                                                    <td>{items.accountType}</td>
                                                    <td>{items.currency.currencyCode}</td>
                                                    <td>{items.status}</td>
                                                    <td className="text-center">
                                                        <button
                                                            type="button"
                                                            onClick={() => handleOpenEditModal(items)}
                                                            className="btn btn-link p-0 text-info"
                                                            title="Edit"
                                                        >
                                                            <i className="bi bi-pencil"></i>
                                                        </button>
                                                    </td>
                                                </tr>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan="8" className="text-center text-muted">
                                                    No content found
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </Container>

                        {/* Pagination */}
                        <div className="d-flex flex-column align-items-center mt-3">
                            <AdvancedPagination
                                currentPage={currentPage}
                                totalPages={totalPages}
                                onPageChange={handlePageChange}
                                pageSize={pageSize}
                                totalItems={totalItems}
                                handlePageSizeChange={handlePageSizeChange}
                                filter={filter}
                            />
                        </div>
                    </>
                }

                {/* Filter Modal */}
                <FilterUserModal
                    show={showFilterModal}
                    handleClose={handleCloseFilterModal}
                    searchData={localFilter} // use localFilter
                    handleChange={handleFilterChange}
                    handleSearch={handleFilterSearch}
                    handleCancel={handleClear}
                    fields={filterFields}
                />

                {/* Delete Modal */}
                <ConfirmModal
                    show={showDeleteModal}
                    handleClose={() => setShowDeleteModal(false)}
                    handleConfirm={deleteAccount}
                    title="Delete Account"
                    body="Are you sure you want to delete this account?"
                />

                {/* Toast */}
                <ToastMessage
                    show={toast.show}
                    onClose={() => setToast({ ...toast, show: false })}
                    message={toast.message}
                    bg={toast.bg}
                />

                {/* Edit Account Modal */}
                <EditPageModal
                    show={showEditModal}
                    handleClose={() => handleCloseEditModal(false, "")}
                    title="Edit Account"
                >
                    {selectedAccount && <EditAccount account={selectedAccount} onClose={handleCloseEditModal} />}
                </EditPageModal>
            </div>
        </>
    );
}

export default AccountDashboard;
