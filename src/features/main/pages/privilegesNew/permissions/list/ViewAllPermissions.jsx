import React, { useCallback, useEffect, useState } from "react";
import { Container } from "react-bootstrap";
import { useDispatch, useSelector } from "react-redux";
import { toast } from "react-toastify";
import EditPageModal from "../../../../../../components/EditPageModal";
import {
  getPermissionById,
  savePermissionsByRoleId,
} from "../../../../../../redux/slices/privilegeSlice";
import { fetchAllRolesDropdown } from "../../../../../../redux/slices/roleSlice";
import { PrivilegesDashboard } from "../../PrivilegesDashboard";
import { UpdatePermissions } from "../update/UpdatePermissions";

export const ViewAllPermissions = () => {
  const dispatch = useDispatch();
  const { selectedPermission, addLoading } = useSelector((state) => state.privilege);
  const { roles } = useSelector((state) => state.role);

  const [selectedRole, setSelectedRole] = useState(sessionStorage.getItem("roleId") || "");
  const [groupedPermissions, setGroupedPermissions] = useState({});
  const [selectAll, setSelectAll] = useState(false);
  const [openIndex, setOpenIndex] = useState(null);

  const [showModal, setShowModal] = useState(false);
  const [title, setTitle] = useState(null);
  const [modalComponent, setModalComponent] = useState(false);

  // Search state
  const [searchName, setSearchName] = useState("");

  // Fetch all roles
  useEffect(() => {
    dispatch(fetchAllRolesDropdown());
  }, [dispatch]);

  // Fetch permissions for selected role
  useEffect(() => {
    if (selectedRole) {
      dispatch(getPermissionById(selectedRole));
    }
  }, [dispatch, selectedRole]);

  // Group permissions
  useEffect(() => {
    if (!selectedPermission || selectedPermission.length === 0) {
      setGroupedPermissions({});
      return;
    }

    const updated = {};

    // Replace forEach with for...of
    for (const perm of selectedPermission) {
      const moduleName = perm.key || "Unknown";

      const valuesWithSelected =
        perm.values?.map((val) => ({
          ...val,
          selected: val.permissionType === "YES",
        })) || [];

      updated[moduleName] = {
        values: valuesWithSelected,
        idMap: {},
      };

      // Replace nested forEach with for...of
      if (perm.values) {
        for (const val of perm.values) {
          const method = val.httpMethod?.toLowerCase();
          if (method) {
            updated[moduleName][method] = val.permissionType === "YES";
            updated[moduleName].idMap[method] = val.id;
          }
        }
      }
    }

    setGroupedPermissions(updated);
  }, [selectedPermission]);


  // Toggle individual checkbox
  const handleCheckboxChange = (module, action) => {
    setGroupedPermissions((prev) => ({
      ...prev,
      [module]: {
        ...prev[module],
        [action]: !prev[module][action],
      },
    }));
  };

  // Toggle Select All
  const handleSelectAll = () => {
    const newSelectAll = !selectAll;
    setSelectAll(newSelectAll);

    setGroupedPermissions((prev) => {
      const updated = {};
      for (const module of Object.keys(prev)) {
        updated[module] = {
          ...prev[module],
          get: newSelectAll,
          post: newSelectAll,
          put: newSelectAll,
          delete: newSelectAll,
          values: (prev[module].values || []).map((val) => ({
            ...val,
            selected: newSelectAll,
          })),
        };
      };
      return updated;
    });
  };

  // Handle Save
  const handleSave = () => {
    if (!selectedPermission || selectedPermission.length === 0) {
      toast.error("No permissions found for update");
      return;
    }

    if (!selectedRole) {
      toast.error("Role Id not found");
      return;
    }

    const updates = getPermissionUpdates(selectedPermission, groupedPermissions);

    dispatch(savePermissionsByRoleId({ id: selectedRole, data: updates }))
      .unwrap()
      .then((res) => {
        toast.success(res.message);
        dispatch(getPermissionById(selectedRole));
      })
      .catch((err) => {
        toast.error(err?.message || "Error updating permissions");
      });
  };

  // Extracted helper — reduces nesting
  const getPermissionUpdates = (permissions, grouped) => {
    const updates = [];

    for (const perm of permissions) {
      const moduleName = perm.key;
      const moduleValues = grouped[moduleName]?.values || [];

      for (const val of perm.values || []) {
        const updatedValue = moduleValues.find((v) => v.id === val.id);
        updates.push({
          id: val.id,
          permissionType: updatedValue?.selected ? "YES" : "NO",
        });
      }
    }

    return updates;
  };

  //Live Filtering (no button needed)
  const filteredPermissions = Object.keys(groupedPermissions)
    .filter((key) => key.toLowerCase().includes(searchName.toLowerCase()))
    .reduce((obj, key) => {
      obj[key] = groupedPermissions[key];
      return obj;
    }, {});

  const openModal = useCallback((component, modalTitle) => {
    setShowModal(true);
    setTitle(modalTitle);
    setModalComponent(component);
  }, []);

  const METHOD_LABELS = {
    get: "View",
    post: "Edit",
    put: "Update",
    delete: "Delete",
  };


  return (
    <>
      <PrivilegesDashboard />
      <Container fluid className="mb-3">
        <div className="row align-items-center">
          <div className="col-auto">
            <h5 className="mainContent mb-0">Privilege Permissions</h5>
          </div>

          <div className="col-auto d-flex align-items-center">
            <h6 className="mb-0 ms-lg-5">Select Role</h6>
            <select
              className="form-select ms-3"
              style={{ width: "200px" }}
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value)}
            >
              <option value="">-- Select --</option>
              {roles.roleList?.map((role) => (
                <option key={role.id} value={role.id}>
                  {role.name}
                </option>
              ))}
            </select>
          </div>

          {/* Live Search Section */}
          <div className="col-auto d-flex align-items-center justify-content-end gap-2">
            <input
              type="text"
              placeholder="Search by privilege name..."
              className="form-control"
              style={{ width: "250px" }}
              value={searchName}
              onChange={(e) => setSearchName(e.target.value)}
            />
            {searchName && (
              <button
                className="btn btn-danger"
                onClick={() => setSearchName("")}
              >
                Clear
              </button>
            )}
          </div>

        </div>
        <div className="mt-2">
          <input type="checkbox" checked={selectAll} onChange={handleSelectAll} className="me-2 form-check-input border-secondary" />Select All
        </div>
      </Container>

      {/* Permissions Table */}
      <div
        style={{
          width: "85vw",
          overflowX: "auto",
          overflowY: "auto",
          maxHeight: "50vh",
          whiteSpace: "nowrap",
          marginLeft: "auto",
          marginRight: "auto",
        }}
        className="scrollableContainer"
      >
        {Object.keys(filteredPermissions).length === 0 ? (
          <div className="text-center text-danger py-3">
            No permissions found.
          </div>
        ) : (
          Object.keys(filteredPermissions).map((moduleName) => (
            <div
              key={moduleName}
              className="border border-gray-500 rounded mb-2 relative"
            >
              <button
                onClick={() =>
                  setOpenIndex((prevIndex) =>
                    prevIndex === moduleName ? null : moduleName
                  )
                }
                className="w-full text-left px-4 py-3 bg-gray-100 hover:bg-gray-200 flex items-center justify-between"
              >
                <span className="fw-bold">{moduleName}</span>
                <div className="flex items-center gap-12">
                  <input
                    type="checkbox"
                    className="me-2 form-check-input border-secondary"
                    onClick={(e) => e.stopPropagation()}
                    checked={
                      filteredPermissions[moduleName].get &&
                      filteredPermissions[moduleName].post &&
                      filteredPermissions[moduleName].put &&
                      filteredPermissions[moduleName].delete
                    }
                    onChange={(e) => {
                      e.stopPropagation();
                      const isChecked = e.target.checked;
                      setGroupedPermissions((prev) => ({
                        ...prev,
                        [moduleName]: {
                          ...prev[moduleName],
                          get: isChecked,
                          post: isChecked,
                          put: isChecked,
                          delete: isChecked,
                          values: (prev[moduleName].values || []).map((v) => ({
                            ...v,
                            selected: isChecked,
                          })),
                        },
                      }));
                    }}
                  />
                  <button onClick={(e) => {
                    e.stopPropagation();
                    openModal(
                      <UpdatePermissions
                        privilegeId={filteredPermissions[moduleName].idMap.post}
                      />,
                      "Update Permissions"
                    );
                  }}>
                    <i
                      className="bi bi-pencil text-info cursor-pointer"
                      title="Edit"

                    ></i>
                  </button>
                  <svg
                    className={`w-5 h-5 transform transition-transform duration-200 ${openIndex === moduleName ? "rotate-180" : ""
                      }`}
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M19 9l-7 7-7-7"
                    />
                  </svg>
                </div>
              </button>

              {openIndex === moduleName && (
                <div className="px-4 py-3 bg-white border-t border-gray-300">
                  <div className="grid grid-cols-4 gap-4 p-3 bg-white rounded">
                    {(filteredPermissions[moduleName]?.values || []).map(
                      (perm) => {
                        // const label = perm.endpointName || perm.httpMethod;
                        const method = perm.httpMethod?.toLowerCase();
                        const label = METHOD_LABELS[method] || perm.endpointName || perm.httpMethod;
                        return (
                          <label key={perm.id} className="flex items-center gap-2">
                            <input
                              type="checkbox"
                              className="form-checkbox h-4 w-4 text-blue-500 align-middle"
                              checked={perm.selected || false}
                              onChange={() => {
                                setGroupedPermissions((prev) => {
                                  const newValues = prev[moduleName].values.map(
                                    (v) =>
                                      v.id === perm.id
                                        ? { ...v, selected: !v.selected }
                                        : v
                                  );
                                  return {
                                    ...prev,
                                    [moduleName]: {
                                      ...prev[moduleName],
                                      values: newValues,
                                    },
                                  };
                                });

                                handleCheckboxChange(
                                  moduleName,
                                  perm.httpMethod.toLowerCase()
                                );
                              }}
                            />
                            <span className="text-sm leading-4 align-middle ms-2 capitalize">
                              {label}
                            </span>
                          </label>
                        );
                      }
                    )}
                  </div>
                </div>
              )}
            </div>
          ))
        )}
      </div>

      <div className="flex justify-end mt-1">
        <button
          className="bg-green-800 hover:bg-green-900 text-white font-semibold py-2 px-4 rounded"
          onClick={handleSave}
        >
          {addLoading ? (<span className="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>) : "Save"}
          {addLoading && "Saving"}
        </button>
      </div>

      {/* Modal */}
      <EditPageModal
        show={showModal}
        handleClose={() => setShowModal(false)}
        title={title}
      >
        {React.cloneElement(modalComponent, {
          handleClose: () => setShowModal(false),
        })}
      </EditPageModal>
    </>
  );
};

