import React, { useCallback, useEffect, useState } from 'react';
import { Button } from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import { EditPageModal } from '../../../../../components/EditPageModal';
import { editPrivilegesById, getPrivilegesByRoleId } from '../../../../../redux/slices/privilegeSlice';
import '../../accounts/list/AccountDashboard.css';
import { CreatePrivilegesPermissions } from '../create/CreatePrivilegesPermissions';
import { UpdatePrivilegesPermissions } from '../update/UpdatePrivilegesPermissions';
import { fetchAllRoles } from '../../../../../redux/slices/roleSlice';
import { ToastMessage } from '../../../../../components/ToastMessage';
import Container from "react-bootstrap/Container";
import styles from "./PrivilegesPermissions.module.css";

export const PrivilegesPermissions = () => {

    const dispatch = useDispatch();
    const { privilegeDetails, addLoading } = useSelector((state) => state.privilege);
    const { roles } = useSelector((state) => state.role);
    const [selectAll, setSelectAll] = useState(false);

    const [selectedRole, setSelectedRole] = useState(sessionStorage.getItem("roleId") || "");
    const [groupedPermissions, setGroupedPermissions] = useState({});
    const [showModal, setShowModal] = useState(false);
    const [title, setTitle] = useState(null);
    const [modalComponent, setModalComponent] = useState(false);
    const [toast, setToast] = useState({ show: false, message: "", bg: "success" });

    const openModal = useCallback((component, modalTitle) => {
        setShowModal(true);
        setTitle(modalTitle);
        setModalComponent(component);
    }, []);

    // Fetch permissions whenever selectedRole changes
    useEffect(() => {
        if (selectedRole) {
            dispatch(getPrivilegesByRoleId(selectedRole));
        }
    }, [dispatch, selectedRole]);

    // Group permissions by module
    useEffect(() => {
        if (!Array.isArray(privilegeDetails) || privilegeDetails.length === 0) return;
        const grouped = {};
        privilegeDetails.forEach((perm) => {
            const moduleName = perm.privileges.name;
            if (!grouped[moduleName]) grouped[moduleName] = {};
            grouped[moduleName][perm.endpoint.httpMethod.toLowerCase()] =
                perm.permissionType === "YES";
            grouped[moduleName]["idMap"] = grouped[moduleName]["idMap"] || {};
            grouped[moduleName]["idMap"][perm.endpoint.httpMethod.toLowerCase()] = perm.id;
        });
        setGroupedPermissions(grouped);
    }, [privilegeDetails]);

    const handleCheckboxChange = (module, action, e, id) => {
        setGroupedPermissions((prev) => ({
            ...prev,
            [module]: {
                ...prev[module],
                [action]: !prev[module][action],
            },
        }));
    };

    const handleSave = () => {
        if (!privilegeDetails || privilegeDetails.length === 0) {
            setToast({ show: true, message: "No previleges found to update", bg: "danger" });
            return;
        }

        const updates = privilegeDetails.map((perm) => {
            const moduleName = perm.privileges?.name;
            const action = perm.endpoint?.httpMethod?.toLowerCase();
            const updatedPermissionType =
                groupedPermissions[moduleName]?.[action] !== undefined
                    ? (groupedPermissions[moduleName][action] ? "YES" : "NO")
                    : perm.permissionType;

            return {
                ...perm,
                permissionType: updatedPermissionType,
            };
        });

        const roleId = privilegeDetails[0]?.role?.id;
        if (!roleId) {
            setToast({ show: true, message: "Role Id not found", bg: "danger" });
            return;
        }

        dispatch(editPrivilegesById({ id: roleId, data: updates }))
            .unwrap()
            .then((res) => {
                setToast({ show: true, message: res.message, bg: "success" });
                dispatch(getPrivilegesByRoleId(roleId));
            })
            .catch((err) => {
                setToast({ show: true, message: err?.message || "Error updating permissions", bg: "danger" });
            });
    };

    useEffect(() => {
        dispatch(fetchAllRoles());
    }, [dispatch]);


    const handleSelectAll = () => {
        const newSelectAll = !selectAll;
        setSelectAll(newSelectAll);

        const updated = {};
        Object.keys(groupedPermissions).forEach((module) => {
            updated[module] = {
                ...groupedPermissions[module],
                post: newSelectAll,
                get: newSelectAll,
                put: newSelectAll,
                delete: newSelectAll,
                idMap: groupedPermissions[module].idMap
            };
        });
        setGroupedPermissions(updated);
    };

    // Reset Select All whenever role changes or privileges load
    useEffect(() => {
        setSelectAll(false);
    }, [selectedRole, privilegeDetails]);

    const stickyHeaderStyle = {
        position: "sticky",
        top: 0,
        backgroundColor: "#212529",
        color: "white",
        zIndex: 10,
    };

    return (
        <>
            <style>{`
                .scrollableContainer::-webkit-scrollbar {
                width: 12px;
                height: 12px;
                }

                .scrollableContainer::-webkit-scrollbar-thumb {
                background-color: #bebefc;
                border-radius: 6px;
                }

                .scrollableContainer::-webkit-scrollbar-track {
                background: #f1f1f1;
                }

                .scrollableContainer {
                scrollbar-width: thin;
                scrollbar-color: #bebefc #f1f1f1;
                }
            `}</style>
            <div className="container-fluid">
                <Container fluid>
                    <div className="row align-items-center mb-2">
                        <div className={styles.topDivision}>
                            {/* Left side - Title */}
                            <div className="col-auto">
                                <h5 className="mainContent mb-0">Privilege Permissions</h5>
                            </div>
                            <div className={styles.buttonDiv}>
                                {/* Middle - Select Role */}
                                <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                                    <h6 className="mb-0">Select Role</h6>
                                    <select
                                        className="form-select"
                                        name="role"
                                        style={{ width: "200px" }}
                                        value={selectedRole}
                                        onChange={(e) => setSelectedRole(e.target.value)}
                                    >
                                        <option value="">-- Select --</option>
                                        {roles.roleList && roles.roleList.map((role) => (
                                            <option key={role.id} value={role.id}>
                                                {role.name}
                                            </option>
                                        ))}
                                    </select>

                                </div>

                                {/* Right side - Button */}
                                <div>
                                    <Button
                                        variant="success"
                                        onClick={() => openModal(<CreatePrivilegesPermissions />, "Create Privileges")}
                                    >
                                        Create
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div>
                        <input
                            type="checkbox"
                            checked={selectAll}
                            onChange={handleSelectAll}
                            className="me-2 form-check-input" />Select All
                    </div>
                </Container>
                <Container fluid
                    style={{
                        maxHeight: "350px",
                    }}
                    className="scrollableContainer">
                    <div className="mt-2">
                        {addLoading && <div className='spinner'> </div>}
                        {Object.keys(groupedPermissions) &&
                            <div
                                style={{
                                    width: "80vw", // full viewport width
                                    overflowX: "auto", // enable horizontal scroll
                                    overflowY: "auto",
                                    maxHeight: "50vh", // optional: vertical limit
                                    whiteSpace: "nowrap", // prevent wrapping
                                    marginLeft: "auto",
                                    marginRight: "auto",
                                }}
                                className="scrollableContainer"
                            >
                                <table className="table table-bordered table-responsive">
                                    <thead className="table-light">
                                        <tr>
                                            <th style={stickyHeaderStyle}>Module</th>
                                            <th style={stickyHeaderStyle}>Create</th>
                                            <th style={stickyHeaderStyle}>List</th>
                                            <th style={stickyHeaderStyle}>Edit</th>
                                            <th style={stickyHeaderStyle}>Delete</th>
                                            <th style={stickyHeaderStyle}>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {Object.keys(groupedPermissions)
                                            .sort((a, b) => a.localeCompare(b))
                                            .map((module, idx) => (
                                                <tr key={idx}>
                                                    <td className='w-25'>{module}</td>
                                                    {["post", "get", "put", "delete"].map((action) => (
                                                        <td key={action}>
                                                            <input
                                                                type="checkbox" className='privilege-checkbox form-check-input'
                                                                checked={groupedPermissions[module][action] || false}
                                                                onChange={(e) =>
                                                                    handleCheckboxChange(
                                                                        module,
                                                                        action,
                                                                        e,
                                                                        groupedPermissions[module].idMap[action]
                                                                    )
                                                                }
                                                            />
                                                        </td>

                                                    ))}
                                                    <td>
                                                        <button type='button'
                                                            className="btn btn-link text-info"
                                                            title="Edit"
                                                            onClick={() =>
                                                                openModal(
                                                                    <UpdatePrivilegesPermissions
                                                                        id={groupedPermissions[module].idMap.post} />,
                                                                    "Update Privileges"
                                                                )
                                                            }
                                                        >
                                                            <i className="bi bi-pencil"></i>
                                                        </button>
                                                    </td>
                                                </tr>
                                            ))}
                                    </tbody>
                                </table>
                            </div>
                        }

                        <div className="mt-1 d-flex justify-content-end px-1">
                            <Button variant="success" onClick={handleSave} disabled={addLoading}>
                                Save
                            </Button>
                        </div>
                    </div>
                </Container>
                {/* Edit Account Modal */}
                <EditPageModal
                    show={showModal}
                    handleClose={() => setShowModal(false)}
                    title={title}
                >
                    {React.cloneElement(modalComponent, {
                        handleClose: () => setShowModal(false),
                    })}
                </EditPageModal>

                {/* Toast */}
                <ToastMessage
                    show={toast.show}
                    onClose={() => setToast({ ...toast, show: false })}
                    message={toast.message}
                    bg={toast.bg}
                />
            </div>
        </>
    );
};
