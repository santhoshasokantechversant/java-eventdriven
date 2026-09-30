import React, { useCallback, useEffect, useState } from 'react';
import { Button, Container, Table } from 'react-bootstrap';
import { useDispatch, useSelector } from 'react-redux';
import { toast } from 'react-toastify';
import ConfirmModal from '../../../../../../components/ConfirmModal';
import EditPageModal from '../../../../../../components/EditPageModal';
import { deleteEndpointsById, fetchAllEndpoints } from '../../../../../../redux/slices/endpointSlice';
import { PrivilegesDashboard } from '../../PrivilegesDashboard';
import styles from "../../PrivilegesDashboard.module.css";
import { CreateEndpoints } from '../create/CreateEndpoints';
import { UpdateEndpoints } from '../update/UpdateEndpoints';
import AdvancedPagination from '../../../../../../components/CustomPagination';

export const ViewAllEndpoints = () => {
    const dispatch = useDispatch();
    const [showModal, setShowModal] = useState(false);
    const [title, setTitle] = useState(null);
    const [modalComponent, setModalComponent] = useState(null);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deleteId, setDeleteId] = useState(null);
    const [searchName, setSearchName] = useState("");
    const [searchData, setSearchData] = useState({});
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const { listEndpoints, deleteLoading } = useSelector((state) => state.endpoints);

    // Open modal for create / update
    const openModal = useCallback((component, modalTitle) => {
        setShowModal(true);
        setTitle(modalTitle);
        setModalComponent(component);
    }, []);

    // Handle delete modal open
    const handleDeleteModal = (id) => {
        setDeleteId(id);
        setShowDeleteModal(true);
    };

    // Fetch all on mount
    useEffect(() => {
        dispatch(fetchAllEndpoints({ page: currentPage, size: pageSize }));
    }, [dispatch, currentPage, pageSize]);

    // Handle search
    const handleSearch = () => {
        const updatedData = { ...searchData, name: searchName, page: 0 };
        setSearchData(updatedData);
        dispatch(fetchAllEndpoints(updatedData))
            .unwrap()
            .then((res) => {
                if (res.message) toast.success(res.message);
            })
            .catch((err) => {
                toast.error(err?.message || "Failed to search endpoints");
            });
    };

    // Handle clear
    const handleClear = () => {
        setSearchName("");
        const resetData = { size: 10, page: 0 };
        setSearchData(resetData);
        dispatch(fetchAllEndpoints(resetData))
            .unwrap()
            .catch((err) => {
                toast.error(err?.message || "Failed to fetch endpoints");
            });
    };

    // Confirm delete
    const handleConfirmDelete = () => {
        dispatch(deleteEndpointsById(deleteId))
            .unwrap()
            .then((res) => {
                toast.success(res.response?.message || "Deleted Successfully");
                setShowDeleteModal(false);
                dispatch(fetchAllEndpoints(searchData));
            })
            .catch((err) => {
                toast.error(err?.message || "Failed to delete");
            });
    };

    // Handle page change
    const handlePageChange = (newPage) => {
        setCurrentPage(newPage);
        dispatch(fetchAllEndpoints({ ...searchData, page: newPage, size: pageSize }));
    };

    // Handle page size change
    const handlePageSizeChange = (e) => {
        const newSize = Number.parseInt(e.target.value);
        setPageSize(newSize);
        setCurrentPage(0);
        dispatch(fetchAllEndpoints({ ...searchData, page: 0, size: newSize }));
    };

    // Custom scroll bar style
    const stickyHeaderStyle = {
        position: "sticky",
        top: 0,
        backgroundColor: "#212529",
        color: "white",
        zIndex: 10,
    };

    return (
        <>
            <PrivilegesDashboard />

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

            {/* Header section */}
            <Container fluid>
                <div className="row align-items-center mb-4">
                    <div className="col-12 d-flex flex-wrap justify-content-between align-items-center gap-2">
                        <h5>Manage Endpoints</h5>
                        <div className="d-flex flex-wrap gap-2">
                            <input
                                type="text"
                                name="name"
                                id="name"
                                placeholder="Search by endpoint"
                                className={styles.filterInput}
                                value={searchName || ""}
                                onChange={(e) => setSearchName(e.target.value)}
                            />
                            <Button variant="primary" onClick={handleSearch}>Search</Button>
                            <Button variant="danger" onClick={handleClear}>Clear</Button>
                            <Button
                                variant="success"
                                onClick={() => openModal(<CreateEndpoints />, "Create Endpoints")}
                            >
                                Create
                            </Button>
                        </div>
                    </div>
                </div>
            </Container>

            {/* Table section */}
            <Container fluid>
                <div
                    className="scrollableContainer"
                    style={{
                        width: "80vw",
                        overflowX: "auto",
                        overflowY: "auto",
                        maxHeight: "50vh",
                        margin: "0 auto",
                    }}
                >
                    <Table className="table table-bordered table-striped align-middle" style={{ minWidth: "900px" }}>
                        <thead className="table-dark">
                            <tr>
                                <th style={stickyHeaderStyle}>Sl No</th>
                                <th style={stickyHeaderStyle}>Endpoint Name</th>
                                <th style={stickyHeaderStyle}>Backend Url</th>
                                <th style={stickyHeaderStyle}>Http Method</th>
                                <th colSpan={2} style={stickyHeaderStyle}>Options</th>
                            </tr>
                        </thead>
                        <tbody>
                            {listEndpoints?.data?.length > 0 ? (
                                listEndpoints.data.map((item, index) => (
                                    <tr key={item.id}>
                                        <td>{currentPage * pageSize + index + 1}</td>
                                        <td>{item.endponitName ?? "-"}</td>
                                        <td>{item.backendUrl ?? "-"}</td>
                                        <td>{item.httpMethod ?? "-"}</td>
                                        <td>
                                            <Button
                                                className="btn btn-link p-0 text-info"
                                                onClick={() => openModal(
                                                    <UpdateEndpoints endpointsId={item.id} />,
                                                    "Update Endpoints"
                                                )}
                                            >
                                                <i className="bi bi-pencil"></i>
                                            </Button>
                                        </td>
                                        <td>
                                            <Button
                                                className="btn btn-link p-0 text-danger"
                                                onClick={() => handleDeleteModal(item.id)}
                                            >
                                                <i className="bi bi-trash-fill"></i>
                                            </Button>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="8" className="text-center text-muted">
                                        No data found
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </Table>
                </div>
            </Container>

            {/* Pagination */}
            {listEndpoints && listEndpoints.totalPages >= 1 && (
                <div className="d-flex flex-column align-items-center mt-3">
                    <AdvancedPagination
                        currentPage={listEndpoints.currentPage}
                        totalPages={listEndpoints.totalPages}
                        onPageChange={handlePageChange}
                        pageSize={listEndpoints.pageSize}
                        totalItems={listEndpoints.totalItems}
                        handlePageSizeChange={handlePageSizeChange}
                        filter={searchData}
                    />
                </div>
            )}

            {/* Edit Modal */}
            <EditPageModal
                show={showModal}
                handleClose={() => setShowModal(false)}
                title={title}
            >
                {modalComponent &&
                    React.cloneElement(modalComponent, { handleClose: () => setShowModal(false) })}
            </EditPageModal>

            {/* Delete Modal */}
            <ConfirmModal
                show={showDeleteModal}
                handleClose={() => setShowDeleteModal(false)}
                title="Delete Endpoints"
                body={
                    deleteLoading
                        ? "Deleting Endpoint..."
                        : "Are you sure you want to delete this Endpoint?"
                }
                handleConfirm={handleConfirmDelete}
                confirmDisabled={deleteLoading}
            />
        </>
    );
};
