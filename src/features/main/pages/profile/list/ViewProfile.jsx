import React, { useCallback, useEffect, useState } from "react";
import EditProfile from "../update/EditProfile";
import EditPageModal from "../../../../../components/EditPageModal";
import { useDispatch, useSelector } from "react-redux";
import { fetchUserById } from "../../../../../redux/slices/userSlice";
import { ToastMessage } from "../../../../../components/ToastMessage";

export const ViewProfile = () => {

    const dispatch = useDispatch();

    // Modal states
    const [showModal, setShowModal] = useState(false);
    const [modalComponent, setModalComponent] = useState(null);
    const [title, setTitle] = useState("");

    // Toast notification state
    const [toast, setToast] = useState({ show: false, message: "", bg: "success" });

    // Redux store values
    const { userDetails, loading } = useSelector((state) => state.user);

    // Fetch userId from session storage
    const userId = sessionStorage.getItem("userId");

    // Close modal handler
    const closeModal = () => setShowModal(false);

    // Toast trigger function
    const showToast = (message, bg = "success") => {
        setToast({ show: true, message, bg });
    };

    // Fetch user details on page load
    useEffect(() => {
        if (userId) {
            dispatch(fetchUserById(userId));
        }
    }, [dispatch, userId]);

    // Reusable function to open modal with dynamic component + title
    const openModal = useCallback((component, modalTitle) => {
        setModalComponent(component);
        setTitle(modalTitle);
        setShowModal(true);
    }, []);

    // Extracted render logic to remove nested ternary
    const renderProfileContent = () => {
        // Show loading state
        if (loading) {
            return <p>Loading...</p>;
        }
        // When no user data in store
        if (!userDetails) {
            return <p className="text-center text-muted">No data found</p>;
        }

        return (
            <div className="card shadow-sm border-0 rounded-3">
                <div className="card-header bg-dark text-white d-flex justify-content-between align-items-center">
                    <h5 className="mb-0">Profile Page</h5>
                    <span
                        className={`badge ${userDetails?.active ? "bg-success" : "bg-secondary"
                            }`}
                    >
                        {userDetails?.active ? "Active" : "Inactive"}
                    </span>
                </div>

                <div className="card-body">
                    <div className="row mb-3">
                        <div className="col-md-6">
                            <strong>First Name:</strong>
                            <p className="mb-0">{userDetails?.firstName ?? "N/A"}</p>
                        </div>
                        <div className="col-md-6">
                            <strong>Last Name:</strong>
                            <p className="mb-0">{userDetails?.lastName ?? "N/A"}</p>
                        </div>
                    </div>

                    <div className="row mb-3">
                        <div className="col-md-6">
                            <strong>Email:</strong>
                            <p className="mb-0">{userDetails?.email ?? "N/A"}</p>
                        </div>
                        <div className="col-md-6">
                            <strong>User Name:</strong>
                            <p className="mb-0">{userDetails?.userName ?? "N/A"}</p>
                        </div>
                    </div>

                    <div className="row mb-3">
                        <div className="col-md-6">
                            <strong>Date of Birth:</strong>
                            <p className="mb-0">{userDetails?.dateOfBirth ?? "N/A"}</p>
                        </div>
                        <div className="col-md-6">
                            <strong>Mobile Number:</strong>
                            <p className="mb-0">{userDetails?.phoneNumber ?? "N/A"}</p>
                        </div>
                    </div>

                    <div className="row mb-3">
                        <div className="col-md-6">
                            <strong>Country:</strong>
                            <p className="mb-0">{userDetails?.country ?? "N/A"}</p>
                        </div>
                        <div className="col-md-6">
                            <strong>State:</strong>
                            <p className="mb-0">{userDetails?.state ?? "N/A"}</p>
                        </div>
                    </div>

                    <div className="row mb-3">
                        <div className="col-md-6">
                            <strong>City:</strong>
                            <p className="mb-0">{userDetails?.city ?? "N/A"}</p>
                        </div>
                        <div className="col-md-6">
                            <strong>Postal Code:</strong>
                            <p className="mb-0">{userDetails?.postalCode ?? "N/A"}</p>
                        </div>
                    </div>

                    <div className="row mb-3">
                        <div className="col-md-6">
                            <strong>Address:</strong>
                            <p className="mb-0">{userDetails?.address ?? "N/A"}</p>
                        </div>
                    </div>
                </div>

                <div className="card-footer d-flex justify-content-end gap-2">
                    <button
                        className="btn btn-outline-info"
                        onClick={() =>
                            openModal(
                                <EditProfile
                                    profile={userDetails}
                                    closeModal={closeModal}
                                    showToast={showToast}
                                />,
                                "Edit Profile"
                            )
                        }
                    >
                        <i className="bi bi-pencil me-2"></i>Edit Profile
                    </button>
                </div>
            </div>
        );
    };

    return (
        <div className="container my-4">
            {renderProfileContent()}

            {/* Edit/Create Modal */}
            <EditPageModal show={showModal} handleClose={closeModal} title={title}>
                {modalComponent}
            </EditPageModal>

            <ToastMessage
                show={toast.show}
                onClose={() => setToast({ ...toast, show: false })}
                message={toast.message}
                bg={toast.bg}
            />
        </div>
    );
};

export default ViewProfile;
