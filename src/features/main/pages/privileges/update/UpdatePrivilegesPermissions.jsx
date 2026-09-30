import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { editPermissionsById, getPermissionById } from '../../../../../redux/slices/privilegeSlice';
import { toast } from "react-toastify";

export const UpdatePrivilegesPermissions = ({ id, handleClose }) => {
    const dispatch = useDispatch();
    const { selectedPermission, addLoading } = useSelector((state) => state.privilege);

    const [slugName, setSlugName] = useState("");
    const [sideNav, setSideNav] = useState("");

    // Load permission when modal opens
    useEffect(() => {
        if (id) dispatch(getPermissionById(id));
    }, [dispatch, id]);

    useEffect(() => {
        if (selectedPermission) {
            setSideNav(selectedPermission.sideNav || "");
        }
    }, [selectedPermission]);

    // Populate slugName when selectedPermission changes
    useEffect(() => {
        if (selectedPermission) {
            setSlugName(selectedPermission?.slugName || "");
        }
    }, [selectedPermission]);

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!selectedPermission) return;

        // Create updated object
        const updatedData = {
            ...selectedPermission,
            slugName,
            sideNav
        };

        dispatch(editPermissionsById({ id, data: updatedData }))
            .unwrap()
            .then((response) => {
                toast.success(response.message);
                handleClose();
                dispatch(getPrivilegesByRoleId(id));
            })
            .catch((err) => {
                console.error(err?.message || "Error updating permission");
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
                        <label className="form-label">Privilege Name</label>
                        <input
                            type="text"
                            className="form-control"
                            value={selectedPermission?.privileges?.name || ""}
                            readOnly
                        />
                    </div>

                    {/* Description */}
                    <div className="col-md-6 mb-3">
                        <label className="form-label">Description</label>
                        <input
                            type="text"
                            className="form-control"
                            value={selectedPermission?.privileges?.description || ""}
                            readOnly
                        />
                    </div>
                </div>

                <div className="row">
                    {/* Slug Name (editable) */}
                    <div className="col-md-6 mb-3">
                        <label className="form-label">Slug Name</label>
                        <input
                            type="text"
                            className="form-control"
                            value={slugName}
                            onChange={(e) => setSlugName(e.target.value)}
                        />
                    </div>

                    {/* End Point */}
                    <div className="col-md-6 mb-3">
                        <label className="form-label">End Point</label>
                        <input
                            type="text"
                            className="form-control"
                            value={selectedPermission?.privileges?.url || ""}
                            readOnly
                        />
                    </div>
                </div>
                <div className="row">
                    <div className="col-md-6 mb-3">
                        <label className="form-label">Side Navbar</label>
                        <select
                            className="form-select"
                            value={sideNav}
                            onChange={(e) => setSideNav(e.target.value)}
                        >
                            <option value="">-- Select --</option>
                            <option value="YES">YES</option>
                            <option value="NO">NO</option>
                        </select>
                    </div>
                </div>



                {/* Buttons */}
                <div className="d-flex justify-content-end mt-4">
                    <button type="submit" className="btn btn-success px-4" disabled={addLoading}>
                        {addLoading ? "Updating..." : "Update"}
                    </button>
                    <button type="button" className="btn btn-danger px-4 ms-2" onClick={handleClose}>
                        Cancel
                    </button>
                </div>
            </form>
        </div>
    );
};
