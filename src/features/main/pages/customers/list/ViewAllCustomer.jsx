import Container from "react-bootstrap/Container";
import { useCallback, useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import ConfirmModal from "../../../../../components/ConfirmModal";
import { EditPageModal } from "../../../../../components/EditPageModal";
import {
  deleteCustomer,
  fetchAllCustomers,
  setFilter,
  setPage,
} from "../../../../../redux/slices/customerSlice";
import { CreateCustomer } from "../create/CreateCustomer";
import EditCustomer from "../update/EditCustomer";
import FilterUserModal from "../../../../../components/FilterUserModal";
import AdvancedPagination from "../../../../../components/CustomPagination";
import styles from "./ViewAllCustomer.module.css";

export const ViewAllCustomer = () => {

  // Define fields available for customer filtering in the modal
  const customerFilterFields = [
    { name: "firstName", placeholder: "Search by First Name" },
    { name: "email", placeholder: "Search by Email" },
    { name: "phoneNumber", placeholder: "Search by Mobile Number" },
  ];

  const dispatch = useDispatch();
  const navigate = useNavigate();

  // Extract customer state data from Redux store
  const {
    customers,
    currentPage,
    totalPages,
    totalItems,
    pageSize,
    filter,
    loading,
    message,
  } = useSelector((state) => state.customers);

  // Local filter state for modal input
  const [localFilter, setLocalFilter] = useState({
    firstName: "",
    email: "",
    phoneNumber: "",
  });

  // Modal visibility management and selected component
  const [showModal, setShowModal] = useState(false);
  const [modalComponent, setModalComponent] = useState(null);
  const [modalTitle, setModalTitle] = useState("");

  const [deleteId, setDeleteId] = useState(null);
  // Delete modal state
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  // Filter modal state
  const [showFilterModal, setShowFilterModal] = useState(false);

  // Fetch customers whenever Redux filter changes
  useEffect(() => {
    dispatch(fetchAllCustomers(filter));
  }, [filter, dispatch]);

  // Show success toast message when customer actions complete
  useEffect(() => {
    if (message) {
      toast.success(message);
    }
  }, [message]);

  // Open modal dynamically with selected component and title
  const openModal = useCallback((component, title) => {
    setModalComponent(component);
    setModalTitle(title);
    setShowModal(true);
  }, []);

  // Show delete confirmation modal
  const handleDeleteModal = useCallback((id) => {
    setDeleteId(id);
    setShowDeleteModal(true);
  }, []);

  // Delete customer and refresh list
  const handleDeleteCustomer = useCallback(async () => {
    setShowDeleteModal(false);
    try {
      await dispatch(deleteCustomer(deleteId)).unwrap();
      dispatch(fetchAllCustomers(filter));
    } catch (err) {
      toast.error(err);
      dispatch(fetchAllCustomers(filter));
    }
  }, [dispatch, deleteId, filter]);

  // Modal filter input change
  const handleSearchChange = (e) => {
    const { name, value } = e.target;
    setLocalFilter((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // Apply filter from modal
  const handleSearch = () => {
    dispatch(setFilter({ ...filter, ...localFilter, page: 0 }));
    dispatch(setPage(0));
    setShowFilterModal(false);
  };

  // clear the search filter
  const handleCancel = () => {
    const cleared = { firstName: "", email: "", phoneNumber: "" };
    setLocalFilter(cleared);
    dispatch(setFilter({ ...filter, ...cleared, page: 0 }));
    dispatch(setPage(0));
    setShowFilterModal(false);
    dispatch(fetchAllCustomers({ ...filter, ...cleared, page: 0, size: 10 }));
  };

  // Page change
  const handlePageChange = (page) => {
    dispatch(setPage(page));
    dispatch(fetchAllCustomers({ ...filter, page }));
  };

  // Page size change
  const handlePageSizeChange = (e) => {
    const newSize = Number.parseInt(e.target.value);
    dispatch(setFilter({ ...filter, size: newSize, page: 0 })); // reset page
    dispatch(setPage(0));
    dispatch(fetchAllCustomers({ ...filter, size: newSize, page: 0 }));
  };

  const stickyHeaderStyle = {
    position: "sticky",
    top: 0,
    backgroundColor: "#212529",
    color: "white",
    zIndex: 10,
  };


  return (
    <div className="container-fluid">
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
        <div className="row align-items-center mb-4">
          <div className={styles.topDivision}>
            <div>
              <h5 style={{ fontFamily: "'Montserrat', sans-serif", fontWeight: 700 }}>
                Manage Customers
              </h5>
            </div>
            <div className={styles.buttonDiv}>
              <button type="button" className="btn btn-primary py-1" onClick={() => setShowFilterModal(true)}>
                Filter
              </button>
              <button type="button" className="btn btn-danger py-1" onClick={handleCancel} disabled={loading}>
                Clear
              </button>
              <button type="button" 
                className="btn btn-success py-1"
                onClick={() =>
                  openModal(<CreateCustomer setShowModal={setShowModal} />, "Create Customer")
                }
              >
                Create
              </button>
            </div>
          </div>
        </div>
      </Container>

      <Container fluid style={{ maxHeight: "350px" }} className="scrollableContainer">
        <div className="shadow-sm">
          {loading ? (
            <p>Loading...</p>
          ) : (
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
                    <th style={stickyHeaderStyle}>Customer No.</th>
                    <th style={stickyHeaderStyle}>First Name</th>
                    <th style={stickyHeaderStyle}>Last Name</th>
                    <th style={stickyHeaderStyle}>Email</th>
                    <th style={stickyHeaderStyle}>Mobile Number</th>
                    <th style={stickyHeaderStyle}>DOB</th>
                    <th style={stickyHeaderStyle}>Status</th>
                    <th colSpan={2} className="text-center" style={stickyHeaderStyle}>
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {customers.length === 0 ? (
                    <tr>
                      <td colSpan={9} className="text-center">
                        No customers found.
                      </td>
                    </tr>
                  ) : (
                    customers.map((customer) => (
                      <tr key={customer.customerId}>
                        <td onClick={() => navigate(`/view-customer/${customer.customerId}`)}>
                          {customer.customerNo}
                        </td>
                        <td>{customer.firstName}</td>
                        <td>{customer.lastName}</td>
                        <td>{customer.email}</td>
                        <td>{customer.phoneNumber}</td>
                        <td>{customer.dateOfBirth}</td>
                        <td>{customer.status}</td>
                        <td className="text-center">
                          <button type="button" 
                            className="btn btn-link p-0 text-info"
                            title="Edit"
                            onClick={() =>
                              openModal(
                                <EditCustomer customer={customer} setShowModal={setShowModal} />,
                                "Edit Customer"
                              )
                            }
                          >
                            <i className="bi bi-pencil"></i>
                          </button>
                        </td>
                        <td className="text-center">
                          <button type="button" 
                            className="btn btn-link p-0 text-danger"
                            title="Delete"
                            onClick={() => handleDeleteModal(customer.customerId)}
                          >
                            <i className="bi bi-trash-fill"></i>
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Pagination */}
        <div className="d-flex flex-column align-items-center mt-3">
          <AdvancedPagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
            pageSize={pageSize}
            totalItems={totalItems}
            handlePageSizeChange={handlePageSizeChange}
            filter={filter}
          />
        </div>
      </Container>

      {/* Modals */}
      <EditPageModal show={showModal} handleClose={() => setShowModal(false)} title={modalTitle}>
        {modalComponent}
      </EditPageModal>

      <ConfirmModal
        show={showDeleteModal}
        handleClose={() => setShowDeleteModal(false)}
        title="Delete Customer"
        body="Are you sure you want to delete this customer?"
        handleConfirm={handleDeleteCustomer}
      />

      <FilterUserModal
        show={showFilterModal}
        handleClose={() => setShowFilterModal(false)}
        searchData={localFilter}
        handleChange={handleSearchChange}
        handleSearch={handleSearch}
        handleCancel={handleCancel}
        fields={customerFilterFields}
      />
    </div>
  );
};

export default ViewAllCustomer;
