import { useState } from "react";
import { Button, Card, Container, Form, Spinner } from "react-bootstrap";
import { useDispatch } from "react-redux";
import { editRoleById, getAllRoles } from "../../../../../redux/slices/roleSlice";
import styles from "./EditRole.module.css";
import { toast } from 'react-toastify';
import PropTypes from "prop-types";

export function EditRole({ roleData, closeModal }) {

  const dispatch = useDispatch();
  const [role, setRole] = useState(roleData || { id: "", name: "", description: "" });
  const [loading, setLoading] = useState(false);

  // Handle input field changes
  const handleChange = (event) => {
    setRole((prev) => ({ ...prev, [event.target.name]: event.target.value }));
  };

  // Handle form submit
  const handleSubmit = (event) => {
    event.preventDefault();
    setLoading(true);
    // Extract id and prepare payload
    const { id, ...payload } = role;
    dispatch(editRoleById({ id, data: payload }))
      .unwrap()
      .then((res) => {
        // Refresh roles list
        dispatch(getAllRoles());
        closeModal();
        toast.success(res.message);
      })
      .catch((err) => {
        closeModal();
        toast.error(err.message);
      });
  };

  return (
    <Container className="w-100 p-3">
      <Card className={`shadow-lg rounded-4 ${styles.editBody}`}>
        <Form className={styles.formBody} onSubmit={handleSubmit}>
          <Form.Group className="mb-1">
            <Form.Label htmlFor="name" className={`fw-semibold ${styles.labelStyle}`}>Name</Form.Label>
            <Form.Control
              type="text"
              id="name"
              name="name"
              value={role.name || ""}
              pattern="[a-zA-Z ]+"
              title="Only letters (A–Z, a–z) and spaces are allowed."
              onChange={handleChange}
              required
            />
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label htmlFor="description" className={`fw-semibold ${styles.labelStyle}`}>Description</Form.Label>
            <Form.Control
              as="textarea"
              id="description"
              name="description"
              value={role.description || ""}
              onChange={handleChange}
            />
          </Form.Group>
          {/* <Form.Group className="mb-3">
            <Form.Label
              htmlFor="roleCreate"
              className={`fw-semibold ${styles.labelStyle}`}
            >
              Role Create
            </Form.Label>
            <Form.Select
              id="roleCreate"
              name="roleCreate"
              value={role.roleCreate || ""}
              onChange={handleChange}
            >
              <option value="">-- Select --</option>
              <option value="YES">Yes</option>
              <option value="NO">No</option>
            </Form.Select>
          </Form.Group> */}

          <div className={styles.submitButton}>
            <Button
              variant="primary"
              type="submit"
              className={styles.submitButton}
              disabled={loading}
            >
              {loading ? (
                <output aria-live="polite" className="d-inline-flex align-items-center">
                  <Spinner
                    as="span"
                    animation="border"
                    size="sm"
                    className="me-2"
                  />
                  Updating...
                </output>
              ) : (
                "Update"
              )}
            </Button>
          </div>
        </Form>
      </Card>
    </Container>
  );
};

//PropTypes validation
EditRole.propTypes = {
  roleData: PropTypes.shape({
    id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    name: PropTypes.string,
    description: PropTypes.string,
    roleCreate: PropTypes.string,
  }),
  closeModal: PropTypes.func,
};

export default EditRole;
