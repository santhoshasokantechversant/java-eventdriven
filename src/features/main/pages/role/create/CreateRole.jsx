import { useEffect, useState } from "react";
import { Button, Card, Container, Form, Spinner } from "react-bootstrap";
import { useDispatch, useSelector } from "react-redux";
import { addRoles, fetchAllRolesDropdown, getAllRoles } from "../../../../../redux/slices/roleSlice";
import styles from "./CreateRole.module.css";
import { toast } from 'react-toastify';
import PropTypes from "prop-types";

export function CreateRole({ closeModal }) {

  // Local state to store form input values
  const [createRoleData, setCreateRoleData] = useState({
    name: "",
    description: "",
    hierarchy: "",
  });

  // Fetch dropdown list of roles
  const roleList = useSelector((state) => state.role.roles.roleList || []);

  // Redux dispatcher
  const dispatch = useDispatch();

  // Loading state for "Add Role" API request
  const { addLoading } = useSelector((state) => state.role);

  // Load hierarchy dropdown roles on component mount
  useEffect(() => {
    dispatch(fetchAllRolesDropdown());
  }, [dispatch]);

  // Update form state dynamically when input changes
  const handleChange = (event) => {
    setCreateRoleData((prev) => ({
      ...prev,
      [event.target.name]: event.target.value,
    }));
  };

  // Submit form → dispatch addRoles → refresh full role list → close modal
  const handleSubmit = (event) => {
    event.preventDefault();
    dispatch(addRoles(createRoleData))
      .unwrap()
      .then((res) => {
        toast.success(res.message);
        // Refresh full role list after creation
        dispatch(getAllRoles()).then(() => {
          closeModal();
        });
      })
      .catch((err) => {
        closeModal();
        toast.error(err.message);
      });
  };

  return (
    <Container>
      <Card className={styles.signupBody}>
        <Form className="p-3" onSubmit={handleSubmit}>
          <Form.Group className="mb-1">
            <Form.Label htmlFor="name">Name</Form.Label>
            <Form.Control
              type="text"
              id="name"
              name="name"
              required
              onChange={handleChange}
              pattern="[a-zA-Z ]+"
              title="Only letters (A–Z, a–z) and spaces are allowed."
            />
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label htmlFor="description">Description</Form.Label>
            <Form.Control
              as="textarea"
              id="description"
              name="description"
              className={styles.description}
              onChange={handleChange}
            />
          </Form.Group>
          <Form.Group>
            <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
              Role
            </Form.Label>
            <Form.Select
              name="hierarchy"
              id="hierarchy"
              value={createRoleData.hierarchy || ""}
              onChange={handleChange}
              disabled={!roleList.length}
            >
              <option value="">-- Select Role --</option>
              {roleList.map((role) => (
                <option key={role.id} value={role.id}>
                  {role.name}
                </option>
              ))}
            </Form.Select>

          </Form.Group>
          <div className={styles.submitButton}>
            <Button
              variant="primary"
              type="submit"
              className={styles.submitButton}
              disabled={addLoading}
            >
              {addLoading ? (
                <output aria-live="polite" className="d-inline-flex align-items-center">
                  <Spinner
                    as="span"
                    animation="border"
                    size="sm"
                    className="me-2"
                  />
                  Saving...
                </output>
              ) : (
                "Save"
              )}
            </Button>
          </div>
        </Form>
      </Card>
    </Container>
  );
};

CreateRole.propTypes = {
  closeModal: PropTypes.func,
}

export default CreateRole;