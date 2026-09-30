import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux';
import { editEndpointsById, fetchAllEndpoints, getEndpointsById } from '../../../../../../redux/slices/endpointSlice';
import { Dropdown } from 'react-bootstrap';
import { fetchPrivilegesDropdown } from '../../../../../../redux/slices/privilegeSlice';
import { toast } from "react-toastify";
import PropTypes from 'prop-types';

export const UpdateEndpoints = ({ handleClose, endpointsId }) => {

    const { addLoading, endpointData } = useSelector((state) => state.endpoints);
    const { listPrivilegesDropdown } = useSelector((state) => state.privilege);

    const dispatch = useDispatch();

    // Local form data state
    const [formData, setFormData] = useState({
        endpointName: "",
        httpMethod: "",
        privilegeId: "",
        backendUrl: "",
    });

    // Fetch endpoint details for editing
    useEffect(() => {
        if (endpointsId) {
            dispatch(getEndpointsById(endpointsId));
        }
    }, [dispatch, endpointsId]);

    // Populate form fields when endpoint data is loaded
    useEffect(() => {
        if (endpointData) {
            setFormData({
                endpointName: endpointData.endponitName || "",
                backendUrl: endpointData.backendUrl || "",
                httpMethod: endpointData.httpMethod || "",
                privilegeId: endpointData.privilegeId || "",
            });
        }
    }, [endpointData]);

    // Handle input change for all form fields
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    // Update endpoint API call
    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            // Update privilege
            const res = await dispatch(editEndpointsById({ id: endpointsId, data: formData })).unwrap();
            // Fetch updated list
            await dispatch(fetchAllEndpoints());
            toast.success(res.message || "Updated successfully");
            handleClose();
        } catch (err) {
            toast.error(err?.message || "Failed to update");
        }
    };

    // Fetch privilege dropdown list
    useEffect(() => {
        dispatch(fetchPrivilegesDropdown());
    }, [dispatch]);

    return (
        <div className="container-fluid">
            <form
                onSubmit={handleSubmit}
                className="p-4 rounded shadow-sm"
                style={{ border: "1px solid #ddd", background: "#fafafa" }}
            >
                <div className="row">
                    {/* Privilege Name */}
                    <div className="col-md-6 mb-3">
                        <label className="form-label" htmlFor='endpointName'>Endpoint Name</label>
                        <input
                            id='endpointName'
                            type="text"
                            className="form-control"
                            name="endpointName"
                            value={formData.endpointName}
                            onChange={handleChange}
                            placeholder="Enter Endpoint Name"
                            required
                        />
                    </div>

                    {/* Backend Url */}
                    <div className="col-md-6 mb-3">
                        <label className="form-label" htmlFor='backendUrl'>Backend Url</label>
                        <input
                            id='backendUrl'
                            type="text"
                            className="form-control"
                            name="backendUrl"
                            value={formData.backendUrl}
                            onChange={handleChange}
                            placeholder="Enter Backend Url"
                        />
                    </div>
                </div>

                <div className="row">
                    {/* Privilege Name Dropdown */}
                    <div className="col-md-6 mb-3">
                        <label className="form-label" htmlFor='privilageName'>Privilege Name</label>
                        <Dropdown
                            onSelect={(eventKey) => {
                                const { id, name } = JSON.parse(eventKey);
                                setFormData((prev) => ({
                                    ...prev,
                                    privilegeId: id,      // store ID for backend
                                    privilegeName: name,  // display name for UI
                                }));
                            }}
                        >
                            <Dropdown.Toggle variant="outline-secondary" className="w-100">
                                {(() => {
                                    // Try to find matching privilege by ID
                                    const matchedPrivilege = (listPrivilegesDropdown || []).find(
                                        (item) => item.id === formData.privilegeId
                                    );

                                    // If match found, display its name; otherwise fallback
                                    return matchedPrivilege
                                        ? matchedPrivilege.privilegeName
                                        : formData.privilegeName || "-- Select Privilege Name --";
                                })()}
                            </Dropdown.Toggle>

                            <Dropdown.Menu>
                                {(listPrivilegesDropdown || []).map((item) => (
                                    <Dropdown.Item
                                        key={item.id}
                                        eventKey={JSON.stringify({
                                            id: item.id,
                                            name: item.privilegeName,
                                        })}
                                    >
                                        {item.privilegeName}
                                    </Dropdown.Item>
                                ))}
                            </Dropdown.Menu>
                        </Dropdown>
                    </div>

                    {/* Http Method */}
                    <div className="col-md-6 mb-3">
                        <label className="form-label" htmlFor='httpMethod'>HTTP Method</label>
                        <Dropdown onSelect={(value) => setFormData((prev) => ({ ...prev, httpMethod: value }))}>
                            <Dropdown.Toggle variant="outline-secondary" className="w-100">
                                {formData.httpMethod || "-- Select HTTP Method --"}
                            </Dropdown.Toggle>
                            <Dropdown.Menu>
                                {["get", "post", "put", "delete"].map((method) => (
                                    <Dropdown.Item key={method} eventKey={method}>
                                        {method}
                                    </Dropdown.Item>
                                ))}
                            </Dropdown.Menu>
                        </Dropdown>
                    </div>

                </div>

                {/* Buttons */}
                <div className="d-flex justify-content-end mt-4">
                    <button type="submit" className="btn btn-success px-4">
                        {addLoading ? (<span className="spinner-border spinner-border-sm me-2"></span>) : "Edit"}
                        {addLoading && " Saving..."}
                    </button>
                    <button
                        type="button"
                        className="btn btn-danger px-4 ms-2"
                        onClick={handleClose}
                    >
                        Cancel
                    </button>
                </div>
            </form>
        </div>
    )
};

UpdateEndpoints.propTypes = {
    handleClose: PropTypes.func.isRequired,
    endpointsId: PropTypes.string.isRequired,
}

export default UpdateEndpoints;
