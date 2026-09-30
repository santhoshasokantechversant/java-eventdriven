import { useEffect, useState } from 'react';
import { Dropdown } from "react-bootstrap";
import { useDispatch, useSelector } from 'react-redux';
import { toast } from 'react-toastify';
import { createEndpoints, fetchAllEndpoints } from '../../../../../../redux/slices/endpointSlice';
import { fetchPrivilegesDropdown, getSideNav } from '../../../../../../redux/slices/privilegeSlice';
import PropTypes from "prop-types";

export const CreateEndpoints = ({ handleClose }) => {
    const dispatch = useDispatch();
    const { addLoading, listPrivilegesDropdown } = useSelector((state) => state.privilege);

    // Local form state for endpoint creation
    const [formData, setFormData] = useState({
        endpointName: "",
        httpMethod: "",
        privilegeId: "",
        backendUrl: "",
    });

    // Fetch side-nav and subNames on component mount
    useEffect(() => {
        dispatch(getSideNav());
    }, [dispatch]);

    // Fetch privilege dropdown data on initial load
    useEffect(() => {
        dispatch(fetchPrivilegesDropdown());
    }, [dispatch]);

    // Update form fields when user types or selects a value
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    // Handle form submission
    const handleSubmit = (e) => {
        e.preventDefault();
        dispatch(createEndpoints(formData))
            .unwrap()
            .then((res) => {
                toast.success(res.message || "Endpoint created Successfully");
                dispatch(fetchAllEndpoints());
                handleClose();
            })
            .catch((err) => {
                toast.error(err?.message || "Failed to save");
            });
    };

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
                        <label className="form-label" htmlFor='privilegeName'>Privilege Name</label>
                        <Dropdown
                            onSelect={(eventKey) => {
                                const { id, name } = JSON.parse(eventKey);
                                setFormData((prev) => ({
                                    ...prev,
                                    privilegeId: id,      // value to send on submit
                                    privilegeName: name,  // value to display
                                }));
                            }}
                        >
                            <Dropdown.Toggle variant="outline-secondary" className="w-100">
                                {formData.privilegeName || "-- Select Privilege Name --"}
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
                        {addLoading ? (
                            <output aria-live="polite" className="d-inline-flex align-items-center">
                                <span className="spinner-border spinner-border-sm me-2"></span>Saving...
                            </output>
                        ) : (
                            "Save"
                        )}
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
    );
};

// Add prop types validation
CreateEndpoints.propTypes = {
    handleClose: PropTypes.func.isRequired,
}

export default CreateEndpoints;