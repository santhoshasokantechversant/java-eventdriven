import React, { useCallback, useEffect, useState } from 'react';
import { Button, Container, Table } from 'react-bootstrap';
import ConfirmModal from '../../../../../../components/ConfirmModal';
import EditPageModal from '../../../../../../components/EditPageModal';
import { PrivilegesDashboard } from '../../PrivilegesDashboard';
import styles from "../../PrivilegesDashboard.module.css";
import { CreatePrivileges } from '../create/CreatePrivileges';
import { UpdatePrivileges } from '../update/UpdatePrivileges';
import { useDispatch, useSelector } from 'react-redux';
import { deletePrivilegesById, fetchAllPrivileges } from '../../../../../../redux/slices/privilegeSlice';
import { toast } from 'react-toastify';

export const ViewAllPrivileges = () => {

    const dispatch = useDispatch();

    const { addLoading, listPrivileges, deleteLoading } = useSelector((state) => state.privilege);
    const [searchName, setSearchName] = useState("");

    const [showModal, setShowModal] = useState(false);
    const [title, setTitle] = useState(null);
    const [modalComponent, setModalComponent] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deleteId, setDeleteId] = useState(null);

    /**
    * Opens a modal with a given component and title
    * Wrapped in useCallback to prevent unnecessary re-renders
    */
    const openModal = useCallback((component, modalTitle) => {
        setShowModal(true);
        setTitle(modalTitle);
        setModalComponent(component);
    }, []);

    /**
  * Shows delete confirmation modal for selected privilege
  */
    const handleDeleteModal = (id) => {
        setShowDeleteModal(true);
        setDeleteId(id);
    };

    /**
 * Fetch all privileges on component mount
 */
    useEffect(() => {
        dispatch(fetchAllPrivileges());
    }, [dispatch]);

    /**
    * Handles confirmed deletion of a privilege
    */
    const handleConfirmDelete = () => {
        dispatch(deletePrivilegesById(deleteId))
            .unwrap()
            .then((res) => {
                toast.success(res.response.message || "Deleted Successfully");
                setShowDeleteModal(false);
                dispatch(fetchAllPrivileges());
            })
            .catch((err) => {
                toast.error(err?.message || "Failed to delete");
            });
    }

    /**
   * Filters privileges by name
   */
    const handleSearch = () => {
        dispatch(fetchAllPrivileges({ name: searchName }))
            .unwrap()
            .then((res) => {
                if (res.message) toast.success(res.message);
            })
    };

    /**
   * Clears search and reloads the full list
   */
    const handleClear = () => {
        setSearchName("");
        dispatch(fetchAllPrivileges())
            .unwrap()
            .catch((err) => {
                toast.error(err?.message || "Failed to fetch privileges");
            });
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

            <Container fluid>
                {/* Header/Search/Create */}
                <div className="row align-items-center mb-4">
                    <div className="col-12 d-flex flex-wrap justify-content-between align-items-center gap-2">
                        <h5 className="">Manage Privileges</h5>
                        <div className="d-flex flex-wrap gap-2">
                            <input
                                type="text"
                                name="name"
                                id="name"
                                placeholder="Search by privilege"
                                className={styles.filterInput}
                                value={searchName || ""}
                                onChange={(e) => setSearchName(e.target.value)}
                            />
                            <Button variant="primary" onClick={() => handleSearch()}>
                                Search
                            </Button>
                            <Button variant="danger" onClick={() => handleClear()}>
                                Clear
                            </Button>
                            <Button variant="success" onClick={() => openModal(<CreatePrivileges />, "Create Privileges")}>
                                Create
                            </Button>
                        </div>
                    </div>
                </div>
            </Container>
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
                    {addLoading && <div className='spinner'></div>}
                    {!addLoading &&
                        < Table className="table table-bordered table-striped align-middle">
                            <thead className="table-dark">
                                <tr>
                                    <th className={styles.stickyHeader}>Sl No</th>
                                    <th className={styles.stickyHeader}>Privilege Name</th>
                                    <th className={styles.stickyHeader}>Description</th>
                                    <th className={styles.stickyHeader}>Slug Name</th>
                                    <th className={styles.stickyHeader}>Backend URL</th>
                                    <th className={styles.stickyHeader}>Side Navbar</th>
                                    <th colSpan={2} className={styles.stickyHeader}>Options</th>
                                </tr>
                            </thead>
                            <tbody>
                                {listPrivileges?.data?.length > 0 ? (
                                    listPrivileges.data.map((item, index) => (
                                        <tr key={item.id}>
                                            <td>{index + 1}</td>
                                            <td >{item.privilegeName}</td>
                                            <td >{item.description}</td>
                                            <td>{item.slugName}</td>
                                            <td>{item.backendUrl}</td>
                                            <td>{item.icon}</td>
                                            <td>
                                                <Button className="btn btn-link p-0 text-info" onClick={() => openModal(<UpdatePrivileges privilegeId={item.id} />, "Update Privileges")}>
                                                    <i className="bi bi-pencil" ></i>
                                                </Button>
                                            </td>
                                            <td>
                                                <Button className="btn btn-link p-0 text-danger" onClick={() => handleDeleteModal(item.id)}>
                                                    <i className="bi bi-trash-fill" ></i>
                                                </Button>
                                            </td>
                                        </tr>
                                    ))) :
                                    (
                                        <tr>
                                            <td colSpan="8" className="text-center text-muted">
                                                No content found
                                            </td>
                                        </tr>
                                    )}
                            </tbody>
                        </Table>
                    }
                </div>
            </Container >
            {/* Edit Account Modal */}
            < EditPageModal
                show={showModal}
                handleClose={() => setShowModal(false)}
                title={title}
            >
                {
                    React.cloneElement(modalComponent, {
                        handleClose: () => setShowModal(false),
                    })
                }
            </EditPageModal >

            {/* Delete Modal */}
            < ConfirmModal
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
        </>
    )
}
