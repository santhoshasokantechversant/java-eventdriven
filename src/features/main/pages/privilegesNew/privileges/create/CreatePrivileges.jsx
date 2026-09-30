import React, { useEffect, useState } from 'react';
import { Dropdown } from "react-bootstrap";
import icons from '../../../../../../config/icons';
import { useDispatch, useSelector } from 'react-redux';
import { addPrivilegesPermissions, fetchAllPrivileges, fetchPrivilegesDropdown, getPrivilegesByRoleId, getSideNav } from '../../../../../../redux/slices/privilegeSlice';
import { toast } from 'react-toastify';
import PropTypes from 'prop-types';

export const CreatePrivileges = ({ handleClose }) => {

    const dispatch = useDispatch();

    const { addLoading, listPrivilegesDropdown } = useSelector((state) => state.privilege);

    const [selectedIcon, setSelectedIcon] = useState(null);
    const selectedRole = sessionStorage.getItem("roleId") || "";

    // Form fields state
    const [formData, setFormData] = useState({
        name: "",
        description: "",
        slugName: "",
        icon: "",
        position: "",
        subName: "",
        url: "",
    });

    // Fetch side-nav and subNames on component mount
    useEffect(() => {
        dispatch(getSideNav());
    }, [dispatch]);

    //Fetch dropdown values for sub-privilege selection
    useEffect(() => {
        dispatch(fetchPrivilegesDropdown());
    }, [dispatch]);

    // Handle icon selection
    const handleSelectIcon = (value) => {
        setSelectedIcon(value);
        setFormData((prev) => ({ ...prev, icon: value }));
    };

    // Handle subName selection
    const handleSelectSubName = (eventKey) => {
        const { id, name } = JSON.parse(eventKey);
        setFormData((prev) => ({
            ...prev,
            subName: id,          // send id on submit
            subNameName: name,    // display name in dropdown
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
                toast.success(res.message || "Privileges created Successfully");
                dispatch(fetchAllPrivileges());
                if (selectedRole) {
                    dispatch(getPrivilegesByRoleId(selectedRole));
                }
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
                        <label className="form-label" htmlFor='name'>Privilege Name</label>
                        <input
                            type="text"
                            className="form-control"
                            id='name'
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            placeholder="Enter Privilege Name"
                            required
                        />
                    </div>

                    {/* Description */}
                    <div className="col-md-6 mb-3">
                        <label className="form-label" htmlFor='description'>Description</label>
                        <input
                            type="text"
                            className="form-control"
                            id='description'
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
                        <label className="form-label" htmlFor='slugName'>Slug Name</label>
                        <input
                            type="text"
                            className="form-control"
                            id='slugName'
                            name="slugName"
                            value={formData.slugName}
                            onChange={handleChange}
                            placeholder="Enter Slug Name"
                            required
                        />
                    </div>

                    {/* Icon Dropdown */}
                    <div className="col-md-6 mb-3">
                        <label className="form-label" htmlFor='icon'>Icon</label>
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
                        <label className="form-label" htmlFor='position'>Position</label>
                        <input
                            type="number"
                            className="form-control"
                            id='position'
                            name="position"
                            value={formData.position}
                            onChange={handleChange}
                            placeholder="Enter Position"
                            required
                        />
                    </div>

                    {/* Sub Name Dropdown */}
                    <div className="col-md-6 mb-3">
                        <label className="form-label" htmlFor='subName'>Sub Name</label>
                        <Dropdown onSelect={handleSelectSubName}>
                            <Dropdown.Toggle variant="outline-secondary" className="w-100">
                                {formData.subNameName || "-- Select Sub Name --"}
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


                    <div className="row">
                        {/* End Point */}
                        <div className="col-md-6 mb-3">
                            <label className="form-label" htmlFor='url'>Backend URL</label>
                            <input
                                type="text"
                                className="form-control"
                                id='url'
                                name="url"
                                value={formData.url}
                                onChange={handleChange}
                                placeholder="Enter Backend URL"
                            />
                        </div>
                    </div>
                </div>


                {/* Buttons */}
                <div className="d-flex justify-content-end mt-4">
                    <button type="submit" className="btn btn-success px-4">
                        {addLoading ? (
                            <>
                                <span className="spinner-border spinner-border-sm me-2"></span>Saving...
                            </>
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
CreatePrivileges.propTypes = {
    handleClose: PropTypes.func.isRequired,
}

export default CreatePrivileges;