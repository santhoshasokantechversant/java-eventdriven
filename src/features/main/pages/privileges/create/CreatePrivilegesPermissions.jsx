import React, { useEffect, useState } from 'react';
import { Dropdown } from "react-bootstrap";
import icons from '../../../../../config/icons';
import { useDispatch, useSelector } from 'react-redux';
import { addPrivilegesPermissions, getPrivilegesByRoleId, getSideNav } from '../../../../../redux/slices/privilegeSlice';
import { ToastMessage } from '../../../../../components/ToastMessage';

export const CreatePrivilegesPermissions = ({ handleClose }) => {
    const dispatch = useDispatch();
    const { addLoading, subNames } = useSelector((state) => state.privilege);

    const [selectedIcon, setSelectedIcon] = useState(null);
    const [toast, setToast] = useState({ show: false, message: "", bg: "success" });
    const selectedRole = sessionStorage.getItem("roleId") || "";

    const [formData, setFormData] = useState({
        name: "",
        description: "",
        slugName: "",
        icon: "",
        position: "",
        subName: "",       // ID of the selected subName
        subNameName: "",   // Name of the selected subName for display
        url: "",
    });

    // Fetch side-nav and subNames on component mount
    useEffect(() => {
        dispatch(getSideNav());
    }, [dispatch]);

    // Handle icon selection
    const handleSelectIcon = (value) => {
        setSelectedIcon(value);
        setFormData((prev) => ({ ...prev, icon: value }));
    };

    // Handle subName selection
    const handleSelectSubName = ({ id, name }) => {
        setFormData((prev) => ({
            ...prev,
            subName: id,
            subNameName: name,
        }));
    };

    // Handle text/number input changes
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
        dispatch(addPrivilegesPermissions(formData))
            .unwrap()
            .then((res) => {
                // Show success toast
                setToast({ show: true, message: res.message, bg: "success" });
                if (selectedRole) {
                    dispatch(getPrivilegesByRoleId(selectedRole));
                }
                handleClose(); // optionally close the modal
            })
            .catch((err) => {
                setToast({ show: true, message: err?.message || "Failed to save", bg: "danger" });
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
                        <label htmlFor="privilegeName" className="form-label">Privilege Name</label>
                        <input
                            type="text"
                            className="form-control"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            placeholder="Enter Privilege Name"
                            required
                        />
                    </div>

                    {/* Description */}
                    <div className="col-md-6 mb-3">
                        <label htmlFor='description' className="form-label">Description</label>
                        <input
                            type="text"
                            className="form-control"
                            name="description"
                            value={formData.description}
                            onChange={handleChange}
                            placeholder="Enter Description"
                            required
                        />
                    </div>
                </div>

                <div className="row">
                    {/* Slug Name */}
                    <div className="col-md-6 mb-3">
                        <label htmlFor='slugName' className="form-label">Slug Name</label>
                        <input
                            type="text"
                            className="form-control"
                            name="slugName"
                            value={formData.slugName}
                            onChange={handleChange}
                            placeholder="Enter Slug Name"
                            required
                        />
                    </div>

                    {/* Icon Dropdown */}
                    <div className="col-md-6 mb-3">
                        <label htmlFor='icon' className="form-label">Icon</label>
                        <Dropdown onSelect={handleSelectIcon}>
                            <Dropdown.Toggle variant="outline-secondary" className="w-100">
                                {selectedIcon
                                    ? icons.find((i) => i.value === selectedIcon)?.icon
                                    : "-- Select Icon --"}
                            </Dropdown.Toggle>
                            <Dropdown.Menu>
                                {icons.map((item) => (
                                    <Dropdown.Item
                                        key={item.value}
                                        eventKey={item.value}
                                        className="d-flex justify-content-start"
                                        title={item.value}
                                    >
                                        {item.icon}
                                    </Dropdown.Item>
                                ))}
                            </Dropdown.Menu>
                        </Dropdown>
                    </div>
                </div>

                <div className="row">
                    {/* Position */}
                    <div className="col-md-6 mb-3">
                        <label htmlFor='position' className="form-label">Position</label>
                        <input
                            type="number"
                            className="form-control"
                            name="position"
                            value={formData.position}
                            onChange={handleChange}
                            placeholder="Enter Position"
                            required
                        />
                    </div>

                    {/* Sub Name Dropdown */}
                    <div className="col-md-6 mb-3">
                        <label htmlFor='subName' className="form-label">Sub Name</label>
                        <Dropdown onSelect={(eventKey) => handleSelectSubName(JSON.parse(eventKey))}>
                            <Dropdown.Toggle variant="outline-secondary" className="w-100">
                                {formData.subNameName || "-- Select Sub Name --"}
                            </Dropdown.Toggle>
                            <Dropdown.Menu>
                                {(subNames || []).map((item) => (
                                    <Dropdown.Item
                                        key={item.id}
                                        eventKey={JSON.stringify({ id: item.id, name: item.name })}
                                    >
                                        {item.name || "-- No Name --"}
                                    </Dropdown.Item>
                                ))}
                            </Dropdown.Menu>
                        </Dropdown>
                    </div>
                    <div className="row">
                        {/* End Point */}
                        <div className="col-md-6 mb-3">
                            <label htmlFor='endPoint' className="form-label">End Point</label>
                            <input
                                type="text"
                                className="form-control"
                                name="url"
                                value={formData.url}
                                onChange={handleChange}
                                placeholder="Enter End Point"
                            />
                        </div>
                    </div>
                </div>


                {/* Buttons */}
                <div className="d-flex justify-content-end mt-4">
                    <button type="submit" className="btn btn-success px-4">
                        {addLoading ? (
                            <>
                                <span
                                    className="spinner-border spinner-border-sm me-2"
                                    aria-hidden="true"
                                ></span>Saving... </>
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

            {/* Toast */}
            <ToastMessage
                show={toast.show}
                onClose={() => setToast({ ...toast, show: false })}
                message={toast.message}
                bg={toast.bg}
            />
        </div>
    );
};
