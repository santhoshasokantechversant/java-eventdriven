import { useEffect, useState } from "react";
import { Button, Container } from "react-bootstrap";
import { useDispatch, useSelector } from "react-redux";
import ConfirmModal from "../../../../../components/ConfirmModal";
import AdvancedPagination from "../../../../../components/CustomPagination";
import EditPageModal from "../../../../../components/EditPageModal";
import { toast as notify } from 'react-toastify';
import {
  deleteRoleById,
  getAllRoles,
} from "../../../../../redux/slices/roleSlice";
import CreateRole from "../create/CreateRole";
import EditRole from "../update/EditRole";
import styles from "./ViewAllRoles.module.css";

export function ViewAllRoles() {
  const dispatch = useDispatch();
  const { roles, addLoading } = useSelector((state) => state.role);

  const [searchData, setSearchData] = useState({});
  const [showModal, setShowModal] = useState(false);
  const [deleteId, setDeleteId] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [modalContent, setModalContent] = useState(null);
  const [modalTitle, setModalTitle] = useState("");

  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const totalPages = roles?.totalPages || 1;
  const totalItems = roles?.totalItems || 1;

  // Load roles on component mount and whenever pagination changes
  useEffect(() => {
    dispatch(getAllRoles({ page: currentPage, size: pageSize }));
  }, [dispatch, currentPage, pageSize]);

  // Handle changes in search field (trims spaces and clears if empty)
  const handleChange = (event) => {
    const trimmedValue = event.target.value.trim();
    if (trimmedValue === "") {
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

  // Clear search fields and reload full list
  const handleClick = () => {
    setSearchData({});

    //Reset page size & page number
    setPageSize(10);
    setCurrentPage(0);

    //Fetch with default values
    dispatch(getAllRoles({ page: 0, size: 10 }));
  };


  // Open delete modal and set selected role id
  const handleDeleteModal = (id) => {
    setShowModal(true);
    setDeleteId(id);
  };

  // Open modal for Edit/Create with dynamic component and title
  const handleOpenEditModal = (component, title) => {
    setModalContent(component);
    setShowEditModal(true);
    setModalTitle(title);
  };

  // Close edit modal
  const handleCloseEditModal = () => {
    setShowEditModal(false);
  };

  // Search roles according to searchData
  const handleSearch = () => {
    if (Object.keys(searchData).length > 0) {
      dispatch(getAllRoles(searchData));
    } else {
      dispatch(getAllRoles());
    }
    setCurrentPage(0);
  };

  // Delete selected role
  const deleteRoles = () => {
    dispatch(deleteRoleById(deleteId))
      .unwrap()
      .then((response) => {
        dispatch(getAllRoles(searchData));
        setShowModal(false);
        notify.success(response.message);
      })
      .catch((error) => {
        notify.error(error.message);
      });
  };

  // Handle page change
  const handlePageChange = (page) => {
    setCurrentPage(page);
    dispatch(getAllRoles({ ...searchData, page, size: pageSize }));
  };

  // Page size change
  const handlePageSizeChange = (e) => {
    const newSize = Number.parseInt(e.target.value);
    setPageSize(newSize);
    setCurrentPage(0);
    dispatch(getAllRoles({ ...searchData, page: 0, size: newSize }));
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
      <Container fluid>
        {/* Header/Search/Create */}
        <div className="row align-items-center mb-4">
          <div className="col-12 d-flex flex-wrap justify-content-between align-items-center gap-2">
            <h5 className={styles.heading}>Manage Roles</h5>
            <div className="d-flex flex-wrap gap-2">
              <input
                type="text"
                name="name"
                id="name"
                placeholder="Search by Role"
                className={styles.filterInput}
                value={searchData.name || ""}
                onChange={handleChange}
              />
              <Button variant="primary" onClick={handleSearch}>
                Search
              </Button>
              <Button variant="danger" onClick={handleClick}>
                Clear
              </Button>
              <Button
                variant="success"
                onClick={() =>
                  handleOpenEditModal(
                    <CreateRole
                      closeModal={handleCloseEditModal}
                    />,
                    "Create Role"
                  )
                }
              >
                Create
              </Button>
            </div>
          </div>
        </div>
      </Container>
      <Container
        fluid
        style={{
          maxHeight: "50vh",
        }}
        className="scrollableContainer"
      >
        {/* Roles Table */}
        <div className="shadow-sm">
          {addLoading && <div className="spinner"></div>}
          {roles.data && (
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
              <table className="table table-bordered table-striped align-middle">
                <thead className="table-dark">
                  <tr>
                    <th style={stickyHeaderStyle}>Sl. No.</th>
                    <th style={stickyHeaderStyle}>Role</th>
                    <th style={stickyHeaderStyle}>Description</th>
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
                  {roles.data && roles.data.length > 0 ? (
                    roles.data.map((role, index) => (
                      <tr key={role.id}>
                        <td>{currentPage * pageSize + index + 1}</td>
                        <td>{role.name}</td>
                        <td>{role.description}</td>
                        <td className="text-center">
                          <Button
                            className="btn btn-link p-0 text-info"
                            title="Edit"
                            onClick={() =>
                              handleOpenEditModal(
                                <EditRole
                                  roleData={role}
                                  closeModal={handleCloseEditModal}
                                />,
                                "Edit Role"
                              )
                            }
                          >
                            <i className="bi bi-pencil"></i>
                          </Button>
                        </td>
                        <td className="text-center">
                          <Button
                            type="button"
                            className="btn btn-link p-0 text-danger"
                            title="Delete"
                            onClick={() => handleDeleteModal(role.id)}
                          >
                            <i className="bi bi-trash-fill"></i>
                          </Button>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="5" className="text-center">
                        No roles found
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </Container>

      {/* Pagination */}
      <div className="d-flex flex-column align-items-center mt-3">
        <AdvancedPagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={handlePageChange}
          pageSize={pageSize}
          totalItems={totalItems}
          handlePageSizeChange={handlePageSizeChange}
          filter={searchData}
        />
      </div>

      {/* Delete Confirmation Modal */}
      <ConfirmModal
        show={showModal}
        handleClose={() => setShowModal(false)}
        handleConfirm={deleteRoles}
        title="Delete Role"
        body="Are you sure you want to delete this role?"
      />

      {/* Edit/Create Modal */}
      <EditPageModal
        show={showEditModal}
        handleClose={handleCloseEditModal}
        title={modalTitle}
      >
        {modalContent}
      </EditPageModal>
    </>
  );
}

export default ViewAllRoles;
