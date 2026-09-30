import React, { useEffect, useState } from 'react'
import { fetchUserById, updateNewPassword } from '../../../../../redux/slices/userSlice';
import { useDispatch, useSelector } from 'react-redux';
import { toast } from 'react-toastify';
import { performLogout } from '../../../../../config/logout';

export const ChangePassword = () => {
    // Get logged-in user details from Redux store
    const { userDetails, loading } = useSelector((state) => state.user);

    const userId = sessionStorage.getItem("userId");
    const dispatch = useDispatch();

    // Form state fields
    const [oldPassword, setOldPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [formErrors, setFormErrors] = useState({});

    // Fetch user details on component load
    useEffect(() => {
        if (userId) {
            dispatch(fetchUserById(userId));
        }
    }, [dispatch, userId]);

    // Validate password fields
    const validate = () => {
        const errors = {};
        // Check new password length
        if (!newPassword || newPassword.length < 8) {
            errors.newPassword = "Password must be at least 8 characters long";
        }
        // Check confirm password length
        if (!confirmPassword || confirmPassword.length < 8) {
            errors.confirmPassword = "Password must be at least 8 characters long";
        }
        // Check if new password and confirm password match
        // Only check if both fields have valid length
        if (
            newPassword && confirmPassword &&
            newPassword.length >= 8 && confirmPassword.length >= 8 &&
            newPassword !== confirmPassword
        ) {
            errors.confirmPassword = "Passwords do not match";
        }
        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // Handle password update submit
    const handleSubmit = (e) => {
        e.preventDefault();
        if (!validate()) return;
        const payload = {
            id: userId,
            data: {
                oldPassword,
                newPassword,
                confirmPassword
            },
        };
        // Dispatch API call to update password
        dispatch(updateNewPassword(payload))
            .unwrap()
            .then((res) => {
                setOldPassword("");
                setNewPassword("");
                setConfirmPassword("");
                // Show toast and perform logout after it closes
                toast.success(res.message || "Password updated successfully!", {
                    autoClose: 2000, 
                    onClose: () => {
                        performLogout();
                    },
                });
            })
            .catch((err) => {
                toast.error(err.message || "Failed to update password");
            });

    };

    return (
        <div className="container my-3 d-flex justify-content-center">
            <div className="card shadow-lg border-0 rounded-4" style={{ maxWidth: "450px", width: "100%" }}>
                <div className="card-header bg-dark  text-white text-center py-3 rounded-top-3">
                    <h5 className="mb-0">
                        Change Password
                    </h5>
                </div>
                <div className="card-body p-4">
                    <form onSubmit={handleSubmit}>
                        <div className="mb-3">
                            <label className="form-label fw-semibold" htmlFor='email'>Email</label>
                            <div className="input-group">
                                <input
                                    id='email'
                                    type="email"
                                    className="form-control"
                                    readOnly
                                    value={userDetails?.email || ""}
                                />
                            </div>
                        </div>
                        {/* Old Password */}
                        <div className="mb-3">
                            <label className="form-label fw-semibold" htmlFor='oldPassword'>Old Password <span className='text-danger'>*</span></label>
                            <div className="input-group">
                                <input
                                    type="password"
                                    className="form-control"
                                    placeholder="Enter old password"
                                    id="oldPassword"
                                    name="oldPassword"
                                    required
                                    value={oldPassword}
                                    onChange={(e) => setOldPassword(e.target.value)}
                                    autoComplete="new-password"
                                />
                            </div>
                        </div>

                        {/* New Password */}
                        <div className="mb-3">
                            <label className="form-label fw-semibold" htmlFor='newPassword'>New Password <span className='text-danger'>*</span></label>
                            <div className="input-group">
                                <input
                                    type="password"
                                    id="newPassword"
                                    name="newPassword"
                                    className="form-control"
                                    placeholder="Enter new password"
                                    required
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                />
                                {formErrors.newPassword && (
                                    <div className="invalid-feedback">{formErrors.newPassword}</div>
                                )}
                            </div>
                            <span className='text-danger small'> {formErrors.newPassword}</span>
                        </div>

                        {/* Confirm Password */}
                        <div className="mb-4">
                            <label className="form-label fw-semibold" htmlFor='confirmPassword'>Confirm Password <span className='text-danger'>*</span></label>
                            <div className="input-group">
                                <input
                                    type="password"
                                    id="confirmPassword"
                                    name="confirmPassword"
                                    className="form-control"
                                    placeholder="Re-enter new password"
                                    required
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                />
                                {formErrors.confirmPassword && (
                                    <div className="invalid-feedback">{formErrors.confirmPassword}</div>
                                )}
                            </div>
                            <span className='text-danger small'> {formErrors.confirmPassword}</span>
                        </div>
                    </form>
                    {/* Submit Button */}
                    <div className="w-100 text-center">
                        <button type="submit" className="btn btn-success" disabled={loading} onClick={handleSubmit}>
                            {loading ? "Updating..." : "Update"}
                        </button>
                    </div>

                </div>

            </div>
        </div>

    )
}
