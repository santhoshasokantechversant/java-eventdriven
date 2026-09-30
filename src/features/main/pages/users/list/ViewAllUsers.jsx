import { useCallback, useEffect, useState } from "react";
import { Button, Container, Spinner, Table } from "react-bootstrap";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import ConfirmModal from "../../../../../components/ConfirmModal";
import AdvancedPagination from "../../../../../components/CustomPagination";
import EditPageModal from "../../../../../components/EditPageModal";
import FilterUserModal from "../../../../../components/FilterUserModal";
import { fetchAllRolesDropdown } from "../../../../../redux/slices/roleSlice";
import {
  clearDeleteUserStatus,
  clearUsers,
  deleteUser,
  fetchUsers,
} from "../../../../../redux/slices/userSlice";
import EditUser from "../../users/update/EditUser";
import CreateUser from "../create/CreateUser";
import styles from "./ViewAllUsers.module.css";

export function ViewAllUsers() {

  const roleList = useSelector((state) => state.role.roles.roleList || []);
  
  const userFilterFields = [
    { name: "firstName", placeholder: "Search by First Name" },
    { name: "lastName", placeholder: "Search by Last Name" },
    { name: "userName", placeholder: "Search by Username" },
    { name: "email", placeholder: "Search by Email" },
    {
      name: "roleId",
      placeholder: "Select Role",
      type: "select",
      options: roleList,
    },
  ];

  const dispatch = useDispatch();
  const navigate = useNavigate();

  const [showFilterModal, setShowFilterModal] = useState(false);
  const [searchData, setSearchData] = useState({ size: 10, page: 0 });
  const [showModal, setShowModal] = useState(false);
  const [modalComponent, setModalComponent] = useState(false);
  const [title, setTitle] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteId, setDeleteId] = useState(null);

  const {
    users,
    usersLoading,
    usersError,
    deleteLoading,
    deleteError,
    deleteSuccess,
  } = useSelector((state) => state.user);

  // Handle input filter change
  const handleChange = (event) => {
    const trimmedValue = event.target.value.trim();
    if (trimmedValue == "") {
      setSearchData((prev) => {
        const rest = { ...prev };
        delete rest[event.target.name];
        return rest;
      });

    } else {
      setSearchData((prev) => ({
        ...prev,
        [event.target.name]: trimmedValue,
      }));
    }
  };

  // Apply filter
  const handleSearch = () => {
    dispatch(fetchUsers({ ...searchData, page: 0 }));
  };

  // Reset filter
  const handleCancel = () => {
    setSearchData({});
    dispatch(fetchUsers({ page: 0 }));
  };

  // Open the delete confirmation modal and store the selected role ID
  const handleDeleteModal = (id) => {
    setShowDeleteModal(true);
    setDeleteId(id);
  };

  // Confirm delete
  const handleConfirmDelete = () => {
    if (deleteId) {
      dispatch(deleteUser(deleteId));
    }
  };

  // Open modal with a specific component and title
  const openModal = useCallback((component, modalTitle) => {
    setModalComponent(component);
    setTitle(modalTitle);
    setShowModal(true);
  }, []);

  // Initial load
  useEffect(() => {
    dispatch(fetchUsers(searchData));
    dispatch(fetchAllRolesDropdown());
    return () => dispatch(clearUsers());
  }, [dispatch]);

  // Handle delete success/error
  useEffect(() => {
    if (deleteSuccess) {
      if (deleteSuccess.status === "error") {
        toast.error(deleteSuccess.message || "Failed to Delete User.");
      } else {
        toast.success(deleteSuccess.message || "Deleted successfully");
      }
      setShowDeleteModal(false);
      setDeleteId(null);
      dispatch(clearDeleteUserStatus());
      dispatch(fetchUsers({ ...searchData, page: users?.currentPage || 0 }));
    }

    if (deleteError) {
      toast.error(deleteError || "Something went wrong");
      dispatch(clearDeleteUserStatus());
    }
  }, [deleteSuccess, deleteError, dispatch, searchData, users]);

  // Pagination with filters
  const handlePageChange = (page) => {
    const updatedData = { ...searchData, page };
    setSearchData(updatedData);
    dispatch(fetchUsers(updatedData));
  };

  // Page size change
  const handlePageSizeChange = (e) => {
    const newSize = Number.parseInt(e.target.value);
    const updatedData = { ...searchData, size: newSize, page: 0 };
    setSearchData(updatedData);
    dispatch(fetchUsers(updatedData));
  };

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
      <Container fluid className={styles.content}>
        <Container fluid>
          <div className="row g-4 align-items-center mb-3">
            <div className={styles.topDivision}>
              <div>
                <h5 className={styles.heading}>Manage Users</h5>
              </div>
              <div className={styles.buttonDiv}>
                <Button
                  variant="primary"
                  onClick={() => setShowFilterModal(true)}
                  className={styles.btn}
                >
                  Filter
                </Button>
                <Button
                  variant="danger"
                  onClick={handleCancel}
                  className={styles.btn}
                >
                  Clear
                </Button>
                <Button
                  variant="success"
                  onClick={() =>
                    openModal(
                      <CreateUser setShowModal={setShowModal} />,
                      "Create User"
                    )
                  }
                  className={styles.btn}
                >
                  Create
                </Button>
              </div>
            </div>
          </div>
        </Container>

        {usersLoading ? (
          <div className="text-center my-5">
            <Spinner animation="border" />
          </div>
        ) : usersError ? (
          <div className="text-danger text-center">{usersError}</div>
        ) : (
          <>
            {/* Horizontally scrollable table container */}
            <div
              style={{
                width: "80vw", 
                overflowX: "auto", 
                overflowY: "auto",
                maxHeight: "50vh", 
                whiteSpace: "nowrap",
                marginLeft: "auto",
                marginRight: "auto",
              }}
              className="scrollableContainer"
            >
              <Table
                className="table table-bordered table-striped align-middle"
                style={{ minWidth: "900px" }} 
              >
                <thead className="table-dark">
                  <tr>
                    <th style={stickyHeaderStyle}>User No.</th>
                    <th style={stickyHeaderStyle}>Username</th>
                    <th style={stickyHeaderStyle}>Email</th>
                    <th style={stickyHeaderStyle}>First Name</th>
                    <th style={stickyHeaderStyle}>Last Name</th>
                    <th style={stickyHeaderStyle}>Status</th>
                    <th
                      colSpan={2}
                      className="text-center"
                      style={stickyHeaderStyle}
                    >
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {users?.data?.length > 0 ? (
                    users.data.map((user) => (
                      <tr key={user.id}>
                        <td onClick={() => navigate(`/user-view/${user.id}`)}>
                          {user.userNo}
                        </td>
                        <td>{user.userName}</td>
                        <td>{user.email}</td>
                        <td>{user.firstName}</td>
                        <td>{user.lastName}</td>
                        <td>Active</td>
                        <td>
                          <Button className="btn btn-link p-0 text-info" onClick={() =>
                            openModal(
                              <EditUser
                                id={user.id}
                                setShowModal={setShowModal}
                              />,
                              "Edit User"
                            )
                          }>
                            <i
                              className="bi bi-pencil"
                            ></i>
                            <span className="visually-hidden">Edit User</span>
                          </Button>
                        </td>
                        <td>
                          <Button className="btn btn-link p-0 text-danger" onClick={() => handleDeleteModal(user.id)}>
                            <i
                              className="bi bi-trash-fill"></i>
                          </Button>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="8" className="text-center">
                        No users found
                      </td>
                    </tr>
                  )}
                </tbody>
              </Table>
            </div>

            {/* Pagination */}
            {users && users.totalPages >= 1 && (
              <div className="d-flex flex-column align-items-center mt-3">
                <AdvancedPagination
                  currentPage={users.currentPage}
                  totalPages={users.totalPages}
                  onPageChange={handlePageChange}
                  pageSize={users.pageSize}
                  totalItems={users.totalItems}
                  handlePageSizeChange={handlePageSizeChange}
                  filter={searchData}
                />
              </div>
            )}
          </>
        )}
      </Container>

      {/* Delete Modal */}
      <ConfirmModal
        show={showDeleteModal}
        handleClose={() => setShowDeleteModal(false)}
        title="Delete User"
        body={
          deleteLoading
            ? "Deleting user..."
            : "Are you sure you want to delete this user?"
        }
        handleConfirm={handleConfirmDelete}
        confirmDisabled={deleteLoading}
      />

      {/* Edit/Create Modal */}
      <EditPageModal
        show={showModal}
        handleClose={() => setShowModal(false)}
        title={title}
      >
        {modalComponent}
      </EditPageModal>

      {/* Filter Modal */}
      <FilterUserModal
        show={showFilterModal}
        handleClose={() => setShowFilterModal(false)}
        handleSearch={() => {
          handleSearch();
          setShowFilterModal(false);
        }}
        handleCancel={() => {
          handleCancel();
          setShowFilterModal(false);
        }}
        searchData={searchData}
        handleChange={handleChange}
        fields={userFilterFields}
      />
    </>
  );
}

export default ViewAllUsers;
