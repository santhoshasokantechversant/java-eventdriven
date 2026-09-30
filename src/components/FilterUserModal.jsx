import { Button, Modal } from 'react-bootstrap';
import styles from "../features/main/pages/users/list/ViewAllUsers.module.css";
import PropTypes from 'prop-types';

/**
 * FilterUserModal Component
 *
 * A reusable modal used for filtering user data based on dynamic fields.
 * Fields can be text inputs or dropdown selects. The parent component 
 * provides the filter configuration through the `fields` array.
 *
 * Props:
 * - show: Controls visibility of the modal
 * - handleClose: Callback to close the modal
 * - handleSearch: Callback triggered when user clicks "Search"
 * - handleCancel: Callback for reset/cancel actions
 * - searchData: Current filter values (object)
 * - handleChange: Input/select onChange handler used to update filter state
 * - fields: Array of filter input configurations (name, placeholder, type, options)
 */
const FilterUserModal = ({ 
    show, 
    handleClose, 
    handleSearch, 
    handleCancel, 
    searchData, 
    handleChange, 
    fields = [] 
}) => {

    const handleSearchAndClose = () => {
        handleSearch();
        handleClose();
    };

    return (
        <Modal show={show} onHide={handleClose} centered>
            <Modal.Header closeButton>
                <Modal.Title>Filter</Modal.Title>
            </Modal.Header>

            <Modal.Body>
                <div className="row g-3">
                    {fields.map((field) => (
                        <div className="col-12 col-sm-auto" key={field.name}>
                            {field.type === 'select' ? (
                                <select
                                    name={field.name}
                                    className={`form-control ${styles.filterInput}`}
                                    value={searchData[field.name] || ""}
                                    onChange={handleChange}
                                >
                                    <option value="">{field.placeholder}</option>
                                    {field.options?.map((option) => (
                                        <option key={option.id} value={option.id}>
                                            {option.name}
                                        </option>
                                    ))}
                                </select>
                            ) : (
                                <input
                                    type={field.type || "text"}
                                    name={field.name}
                                    placeholder={field.placeholder}
                                    className={`form-control ${styles.filterInput}`}
                                    value={searchData[field.name] || ""}
                                    onChange={handleChange}
                                />
                            )}
                        </div>
                    ))}
                </div>
            </Modal.Body>

            <Modal.Footer>
                <Button variant="primary" onClick={handleSearchAndClose}>
                    Search
                </Button>
                <Button variant="secondary" onClick={handleCancel}>
                    Cancel
                </Button>
            </Modal.Footer>
        </Modal>
    );
};

/*PropTypes Validation */
FilterUserModal.propTypes = {
    show: PropTypes.bool.isRequired,
    handleClose: PropTypes.func.isRequired,
    handleSearch: PropTypes.func.isRequired,
    handleCancel: PropTypes.func.isRequired,
    searchData: PropTypes.object.isRequired,
    handleChange: PropTypes.func.isRequired,
    fields: PropTypes.arrayOf(
        PropTypes.shape({
            name: PropTypes.string.isRequired,
            placeholder: PropTypes.string,
            type: PropTypes.string,
            options: PropTypes.arrayOf(
                PropTypes.shape({
                    id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
                    name: PropTypes.string
                })
            )
        })
    )
};

export default FilterUserModal;
