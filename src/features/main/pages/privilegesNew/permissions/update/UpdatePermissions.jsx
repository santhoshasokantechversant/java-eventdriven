import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { toast } from 'react-toastify';
import { editPermissionsById, getPermissionByPrevId } from '../../../../../../redux/slices/privilegeSlice';
import PropTypes from 'prop-types';

export const UpdatePermissions = ({ handleClose, privilegeId }) => {

    const dispatch = useDispatch();

    // Extract privilege details & loading state from Redux
    const { addLoading, privilegeDetails } = useSelector((state) => state.privilege);

    // Local form state
    const [formData, setFormData] = useState({
        name: "",
        slugName: "",
        position: "",
        url: "",
        sideNav: "",
    });

    // Fetch side-nav and privilege details on mount
    useEffect(() => {
        if (privilegeId) {
            dispatch(getPermissionByPrevId(privilegeId));
        }
    }, [dispatch, privilegeId]);

    // Populate form when privilegeDetails changes
    useEffect(() => {
        if (privilegeDetails) {
            setFormData({
                name: privilegeDetails.privilegeName || "",
                slugName: privilegeDetails.slugName || "",
                position: privilegeDetails.position || "",
                url: privilegeDetails.backendUrl || "",
                sideNav: privilegeDetails.sideNav || "",
            });
        }
    }, [privilegeDetails]);

    //Handle form input updates
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    //Save updated privilege
    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            // Update privilege
            const res = await dispatch(editPermissionsById({ id: privilegeId, data: formData })).unwrap();
            toast.success(res.message || "Updated successfully");
            // Close modal
            handleClose();
        } catch (err) {
            toast.error(err?.message || "Failed to update");
        }
    };

    return (
        <div className="container-fluid">
            <form onSubmit={handleSubmit} className="p-4 rounded shadow-sm" style={{ border: "1px solid #ddd", background: "#fafafa" }}>
                <div className="row">
                    <div className="col-md-6 mb-3">
                        <label htmlFor='name'>Privilege Name</label>
                        <input type="text" className="form-control" id='name' name="name" value={formData.name} onChange={handleChange} readOnly />
                    </div>
                    <div className="col-md-6 mb-3">
                        <label htmlFor='slugName'>Slug Name</label>
                        <input type="text" className="form-control" id='slugName' name="slugName" value={formData.slugName} onChange={handleChange} required />
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-6 mb-3">
                        <label htmlFor='position'>Position</label>
                        <input type="number" className="form-control" id='position' name="position" value={formData.position} onChange={handleChange} readOnly />
                    </div>
                    <div className="col-md-6 mb-3">
                        <label htmlFor='url'>Backend Url</label>
                        <input type="text" className="form-control" id='url' name="url" value={formData.url} onChange={handleChange} readOnly />
                    </div>
                </div>
                <div className="row">
                    <div className="col-md-6 mb-3">
                        <label htmlFor='sideNav'>SideNav</label>
                        <select
                            className="form-control"
                            id='sideNav'
                            name="sideNav"
                            value={formData.sideNav}
                            onChange={handleChange}
                        >
                            <option value="YES">YES</option>
                            <option value="NO">NO</option>
                        </select>
                    </div>
                </div>

                <div className="d-flex justify-content-end mt-4">
                    <button type="submit" className="btn btn-success px-4">
                        {addLoading ? (<span className="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>) : "Edit"}
                        {addLoading && " Saving..."}
                    </button>
                    <button type="button" className="btn btn-danger px-4 ms-2" onClick={handleClose}>Cancel</button>
                </div>
            </form>
        </div>
    );
};

UpdatePermissions.propTypes = {
    handleClose: PropTypes.func.isRequired,
    privilegeId: PropTypes.string.isRequired,
}

export default UpdatePermissions;
