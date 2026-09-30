import { useEffect, useState } from 'react';
import { Dropdown } from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import { toast } from "react-toastify";
import icons from '../../../../../../config/icons';
import {
    editPrivilegesById,
    fetchAllPrivileges,
    fetchPrivilegesDropdown,
    getPrivilegesByRoleId
} from '../../../../../../redux/slices/privilegeSlice';
import PropTypes from 'prop-types';

export const UpdatePrivileges = ({ handleClose, privilegeId }) => {
    const dispatch = useDispatch();
    const { addLoading, privilegeDetails, listPrivilegesDropdown } = useSelector((state) => state.privilege);

    const [formData, setFormData] = useState({
        name: "",
        description: "",
        slugName: "",
        icon: "",
        position: "",
        subName: "",
        subNameName: "",
        url: "",
    });

    const [selectedIcon, setSelectedIcon] = useState(null);

    // Fetch privilege details
    useEffect(() => {
        if (privilegeId) {
            dispatch(getPrivilegesByRoleId(privilegeId));
        }
    }, [dispatch, privilegeId]);

    // Fetch privileges dropdown list
    useEffect(() => {
        dispatch(fetchPrivilegesDropdown());
    }, [dispatch]);

    // Populate form data when privilege details are available
    useEffect(() => {
        if (privilegeDetails) {
            setFormData((prev) => ({
                ...prev,
                name: privilegeDetails.privilegeName || "",
                description: privilegeDetails.description || "",
                slugName: privilegeDetails.slugName || "",
                icon: privilegeDetails.icon || "",
                position: privilegeDetails.position || "",
                subName: privilegeDetails.parentPrivilegeId || "",
                url: privilegeDetails.backendUrl || "",
            }));
            setSelectedIcon(privilegeDetails.icon || null);
        }
    }, [privilegeDetails]);

    //  Automatically set subNameName (privilege name) after both data sets load
    useEffect(() => {
        if (formData.subName && listPrivilegesDropdown?.length > 0) {
            const matched = listPrivilegesDropdown.find((item) => item.id === formData.subName);
            if (matched) {
                setFormData((prev) => ({
                    ...prev,
                    subNameName: matched.privilegeName,
                }));
            }
        }
    }, [formData.subName, listPrivilegesDropdown]);

    // Automatically updates the corresponding field in formData state.
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    // Handles icon selection from dropdown.
    // Stores selected icon both for display and for form submission.
    const handleSelectIcon = (value) => {
        setSelectedIcon(value);
        setFormData((prev) => ({ ...prev, icon: value }));
    };

    // Handles sub-menu (subName) selection from dropdown.
    // eventKey contains both id and name of the selected sub-privilege.
    // We store the ID for backend submission and name for UI display.
    const handleSelectSubName = (eventKey) => {
        const { id, name } = JSON.parse(eventKey);
        setFormData((prev) => ({
            ...prev,
            subName: id,
            subNameName: name,
        }));
    };

    // Submits the privilege edit form.
    // Sends update request → reloads privileges → shows success toast → closes modal.
    // Automatically handles errors and shows toast message.
    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const res = await dispatch(editPrivilegesById({ id: privilegeId, data: formData })).unwrap();
            await dispatch(fetchAllPrivileges());
            toast.success(res.message || "Updated successfully");
            handleClose();
        } catch (err) {
            toast.error(err?.message || "Failed to update");
        }
    };

    return (
        <div className="container-fluid">
            <form
                onSubmit={handleSubmit}
                className="p-4 rounded shadow-sm"
                style={{ border: "1px solid #ddd", background: "#fafafa" }}
            >
                <div className="row">
                    <div className="col-md-6 mb-3">
                        <label htmlFor='name'>Privilege Name</label>
                        <input
                            type="text"
                            className="form-control"
                            id='name'
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="col-md-6 mb-3">
                        <label htmlFor='description'>Description</label>
                        <input
                            type="text"
                            className="form-control"
                            id='description'
                            name="description"
                            value={formData.description}
                            onChange={handleChange}
                            required
                        />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-6 mb-3">
                        <label htmlFor='slugName'>Slug Name</label>
                        <input
                            type="text"
                            className="form-control"
                            id='slugName'
                            name="slugName"
                            value={formData.slugName}
                            onChange={handleChange}
                            required
                        />
                    </div>
                    <div className="col-md-6 mb-3">
                        <label htmlFor='icon'>Icon</label>
                        <Dropdown onSelect={handleSelectIcon}>
                            <Dropdown.Toggle variant="outline-secondary" className="w-100">
                                {selectedIcon
                                    ? icons.find((i) => i.value === selectedIcon)?.icon
                                    : "-- Select Icon --"}
                            </Dropdown.Toggle>
                            <Dropdown.Menu>
                                {icons.map((i) => (
                                    <Dropdown.Item key={i.value} eventKey={i.value}>
                                        {i.icon}
                                    </Dropdown.Item>
                                ))}
                            </Dropdown.Menu>
                        </Dropdown>
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-6 mb-3">
                        <label htmlFor='position'>Position</label>
                        <input
                            type="number"
                            className="form-control"
                            id='position'
                            name="position"
                            value={formData.position}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    {/*Sub Name Dropdown with Default Value */}
                    <div className="col-md-6 mb-3">
                        <label className="form-label" htmlFor='subName'>Sub Name</label>
                        <Dropdown onSelect={handleSelectSubName}>
                            <Dropdown.Toggle variant="outline-secondary" className="w-100">
                                {(() => {
                                    // Find and display matched privilege name
                                    const matchedPrivilege = (listPrivilegesDropdown || []).find(
                                        (item) => item.id === formData.subName
                                    );
                                    return (
                                        matchedPrivilege?.privilegeName ||
                                        formData.subNameName ||
                                        "-- Select Sub Name --"
                                    );
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
                </div>

                <div className="row">
                    <div className="col-md-6 mb-3">
                        <label htmlFor='url'>Backend URL</label>
                        <input
                            type="text"
                            className="form-control"
                            id='url'
                            name="url"
                            value={formData.url}
                            onChange={handleChange}
                        />
                    </div>
                </div>

                <div className="d-flex justify-content-end mt-4">
                    <button type="submit" className="btn btn-success px-4">
                        {addLoading ? (
                            <output>
                                <span className="spinner-border spinner-border-sm me-2"></span> Saving...
                            </output>
                        ) : (
                            "Edit"
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
UpdatePrivileges.propTypes = {
    handleClose: PropTypes.func.isRequired,
    privilegeId: PropTypes.string.isRequired,
}

export default UpdatePrivileges;
