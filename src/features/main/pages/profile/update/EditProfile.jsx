import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { clearCities, clearStates, fetchCitiesForState, fetchCountriesForRegion, fetchStatesForCountry } from '../../../../../redux/slices/locationSlice';
import { editUser, fetchUserById } from '../../../../../redux/slices/userSlice';
import PropTypes from "prop-types";

export const EditProfile = ({ profile, closeModal, showToast }) => {

    // Get dropdown data from Redux store
    const { countries, states, cities } = useSelector((state) => state.locations);

    // Loading state from customer slice
    const { loading } = useSelector((state) => state.customers);

    const dispatch = useDispatch();

    // Form state for profile update
    const [formData, setFormData] = useState({
        id: "",
        firstName: "",
        lastName: "",
        email: "",
        phoneNumber: "",
        dateOfBirth: "",
        userName: "",
        address: "",
        city: "",
        country: "",
        state: "",
        postalCode: ""
    });

    // Handle input changes
    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    // Handle form submit (update user)
    const handleSubmit = (e) => {
        e.preventDefault();
        const userId = formData.id;
        dispatch(editUser({ id: userId, data: formData }))
            .unwrap()
            .then((res) => {
                closeModal();
                showToast(res?.message || "Role updated successfully!", "success");

                // Refetch updated user data and update form
                dispatch(fetchUserById(userId))
                    .unwrap()
                    .then((updatedProfile) => {
                        setFormData({
                            id: updatedProfile.id || "",
                            firstName: updatedProfile.firstName || "",
                            lastName: updatedProfile.lastName || "",
                            email: updatedProfile.email || "",
                            phoneNumber: updatedProfile.phoneNumber || "",
                            dob: updatedProfile.dateOfBirth || "",
                            userName: updatedProfile.userName || "",
                            address: updatedProfile.address || "",
                            country: updatedProfile.country || "",
                            city: updatedProfile.city || "",
                            state: updatedProfile.state || "",
                            postalCode: updatedProfile.postalCode || "",
                        });
                    });
            })
            .catch((err) => {
                showToast(err?.message || "Failed to update profile.", "danger");
            });
    };

    // Load profile into form when modal opens
    useEffect(() => {
        if (profile) {
            setFormData(profile);
        }
    }, [profile]);

    // Load countries
    useEffect(() => {
        dispatch(fetchCountriesForRegion());
    }, [dispatch]);

    // Load states when country changes
    useEffect(() => {
        if (formData.country) {
            const selectedCountry = countries.find(
                (c) => c.name === formData.country
            );
            if (selectedCountry?.id)
                dispatch(fetchStatesForCountry(selectedCountry.id));
        } else {
            dispatch(clearStates());
            dispatch(clearCities());
        }
    }, [formData.country, countries, dispatch]);

    // Load cities when state changes
    useEffect(() => {
        if (formData.state) {
            const selectedState = states.find((s) => s.name === formData.state);
            if (selectedState?.id) dispatch(fetchCitiesForState(selectedState.id));
        } else {
            dispatch(clearCities());
        }
    }, [formData.state, states, dispatch]);

    return (
        <div>
            <div className="container-fluid">
                <form
                    onSubmit={handleSubmit}
                    className="p-2 rounded shadow-sm"
                    style={{ border: "1px solid #ddd", background: "#fafafa" }}
                >
                    <div className="row g-2 text-black">
                        {/* First name */}
                        <div className="col-md-6">
                            <label className="form-label" htmlFor='firstName'>First Name</label>
                            <input
                                id='firstName'
                                type="text"
                                className="form-control form-control-sm"
                                name="firstName"
                                value={formData.firstName}
                                onChange={handleChange} required
                            />
                        </div>

                        {/* Last name */}
                        <div className="col-md-6">
                            <label className="form-label" htmlFor='lastName'>Last Name</label>
                            <input
                                id='lastName'
                                type="text"
                                className="form-control form-control-sm"
                                name="lastName"
                                value={formData.lastName}
                                onChange={handleChange}
                            />
                        </div>

                        {/* Email */}
                        <div className="col-md-6">
                            <label className="form-label" htmlFor='email'>Email</label>
                            <input
                                id='email'
                                type="email"
                                className="form-control form-control-sm"
                                name="email"
                                value={formData.email}
                                onChange={handleChange}
                                readOnly
                            />
                        </div>
                        <div className="col-md-6">
                            <label className="form-label" htmlFor='country'>User Name</label>
                            <input
                                id='country'
                                type="text"
                                className="form-control form-control-sm"
                                name="userName"
                                value={formData.userName}
                                onChange={handleChange} readOnly
                            />
                        </div>
                        {/* Phone */}
                        <div className="col-md-6">
                            <label className="form-label" htmlFor='phoneNumber'>Phone Number</label>
                            <input
                                id='phoneNumber'
                                type="tel"
                                className="form-control form-control-sm"
                                name="phoneNumber"
                                value={formData.phoneNumber}
                                onChange={handleChange}
                                readOnly
                            />
                        </div>

                        {/* DOB */}
                        <div className="col-md-6">
                            <label className="form-label" htmlFor='dob'>DOB</label>
                            <input
                                id='dob'
                                type="date"
                                className="form-control form-control-sm"
                                name="dateOfBirth"
                                value={formData.dateOfBirth}
                                onChange={handleChange}
                            />
                        </div>

                        {/* Country */}
                        <div className="col-md-6">
                            <label className="form-label" htmlFor='country'>Country</label>
                            <select
                                id="country"
                                className="form-select form-select-sm"
                                name="country"
                                value={formData.country}
                                onChange={handleChange}
                            >
                                <option value="">-- Select --</option>
                                {countries.map((c) => (
                                    <option key={c.id} value={c.name}>
                                        {c.name}
                                    </option>
                                ))}
                            </select>
                        </div>

                        {/* State */}
                        <div className="col-md-6">
                            <label className="form-label" htmlFor='state'>State</label>
                            <select
                                id='state'
                                className="form-select form-select-sm"
                                name="state"
                                value={formData.state}
                                onChange={handleChange}
                            >
                                <option value="">-- Select --</option>
                                {states.map((s) => (
                                    <option key={s.id} value={s.name}>
                                        {s.name}
                                    </option>
                                ))}
                            </select>
                        </div>

                        {/* City */}
                        <div className="col-md-6">
                            <label className="form-label" htmlFor='city'>City</label>
                            <select
                                id='city'
                                className="form-select form-select-sm"
                                name="city"
                                value={formData.city}
                                onChange={handleChange}
                            //   disabled={!formData.state}
                            // disabled
                            >
                                <option value="">-- Select --</option>
                                {cities.map((city) => (
                                    <option key={city.id} value={city.name}>
                                        {city.name}
                                    </option>
                                ))}
                            </select>
                        </div>

                        {/* Postal Code */}
                        <div className="col-md-6">
                            <label className="form-label" htmlFor='postalCode'>Postal Code</label>
                            <input
                                id='postalCode'
                                type="text"
                                className="form-control form-control-sm"
                                name="postalCode"
                                value={formData.postalCode}
                                onChange={handleChange}
                            />
                        </div>

                        {/* Address */}
                        <div className="col-md-6">
                            <label className="form-label" htmlFor='address'>Address</label>
                            <input
                                id='address'
                                type="text"
                                className="form-control form-control-sm"
                                name="address"
                                value={formData.address}
                                onChange={handleChange}
                            />
                        </div>
                    </div>

                    {/* Buttons */}
                    <div className="mt-3 d-flex justify-content-end">
                        <button type="submit" className="btn btn-primary btn-sm px-4" disabled={loading}>
                            {loading ? "Updating..." : "Update"}
                        </button>

                        <button type="button" className="btn btn-danger btn-sm px-4 ms-2" onClick={closeModal}>
                            Cancel
                        </button>
                    </div>
                </form>
            </div>

        </div>
    )
};

EditProfile.propTypes = {
    profile: PropTypes.shape({
        id: PropTypes.string,
        firstName: PropTypes.string,
        lastName: PropTypes.string,
        email: PropTypes.string,
        phoneNumber: PropTypes.string,
        dateOfBirth: PropTypes.string,
        userName: PropTypes.string,
        address: PropTypes.string,
        city: PropTypes.string,
        country: PropTypes.string,
        state: PropTypes.string,
        postalCode: PropTypes.string,
    }),
    closeModal: PropTypes.func.isRequired,
    showToast: PropTypes.func.isRequired,
};
export default EditProfile;