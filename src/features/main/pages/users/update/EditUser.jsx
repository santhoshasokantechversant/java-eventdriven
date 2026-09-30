import PropTypes from "prop-types";
import { useEffect, useState } from "react";
import { Button, Card, Col, Container, Form, Row } from "react-bootstrap";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import {
  fetchCitiesForState,
  fetchCountriesForRegion,
  fetchStatesForCountry,
} from "../../../../../redux/slices/locationSlice";
import { fetchAllRolesDropdown } from "../../../../../redux/slices/roleSlice";
import {
  editUser,
  fetchUserById,
  fetchUsers,
} from "../../../../../redux/slices/userSlice";
import styles from "./EditUser.module.css";

export function EditUser({ id, setShowModal }) {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const roleList = useSelector((state) => state.role.roles.roleList || []);
  const { userDetails, editLoading, editError, editResponse } = useSelector(
    (state) => state.user
  );
  const { countries, states, cities } = useSelector((state) => state.locations);

  const [user, setUser] = useState({
    id: "",
    userName: "",
    email: "",
    firstName: "",
    lastName: "",
    phoneNumber: "",
    dateOfBirth: "",
    address: "",
    city: "",
    state: "",
    postalCode: "",
    country: "",
    roleId: "", // existing role
  });

  const [formErrors, setFormErrors] = useState({});
  const [hasEdited, setHasEdited] = useState(false);

  // Fetch user, countries, roles on mount
  useEffect(() => {
    if (id) dispatch(fetchUserById(id));
    dispatch(fetchCountriesForRegion());
    dispatch(fetchAllRolesDropdown());
  }, [dispatch, id]);

  // Populate form when userDetails is loaded
  useEffect(() => {
    if (userDetails) {
      const data = userDetails.data || userDetails;
      setUser((prev) => ({
        ...prev,
        id: data.id || "",
        userName: data.userName || "",
        email: data.email || "",
        firstName: data.firstName || "",
        lastName: data.lastName || "",
        phoneNumber: data.phoneNumber || "",
        dateOfBirth: data.dateOfBirth || "",
        address: data.address || "",
        city: data.city || "",
        state: data.state || "",
        postalCode: data.postalCode || "",
        country: data.country || "",
        roleId: data.roleId || "", // pre-select user's role
      }));
    }
  }, [userDetails]);

  // Ensure default role is in list
  useEffect(() => {
    if (user.roleId && roleList.length) {
      const roleExists = roleList.find((r) => r.id === user.roleId);
      if (!roleExists) setUser((prev) => ({ ...prev, roleId: "" }));
    }
  }, [roleList, user.roleId]);

  // Load states when country changes
  useEffect(() => {
    if (user.country) {
      const selectedCountry = countries.find((c) => c.name === user.country);
      if (selectedCountry?.id) dispatch(fetchStatesForCountry(selectedCountry.id));
    }
  }, [user.country, countries, dispatch]);

  // Load cities when state changes
  useEffect(() => {
    if (user.state) {
      const selectedState = states.find((s) => s.name === user.state);
      if (selectedState?.id) dispatch(fetchCitiesForState(selectedState.id));
    }
  }, [user.state, states, dispatch]);

  // Navigate after successful edit
  useEffect(() => {
    if (hasEdited && !editLoading && !editError && editResponse) {
      setShowModal(false);
      toast.success(editResponse.message);
      dispatch(fetchUsers({ page: 0, size: 10 }, navigate));
      navigate("/view-all-users");
    }
  }, [hasEdited, editLoading, editError, dispatch, navigate, setShowModal]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setUser((prev) => ({ ...prev, [name]: value }));
    setFormErrors((prev) => ({ ...prev, [name]: "" }));
  };

  const validate = () => {
    const errors = {};
    if (!user.firstName.trim()) errors.firstName = "First name is required";
    if (!user.lastName.trim()) errors.lastName = "Last name is required";
    if (!user.email.trim()) errors.email = "Email is required";
    if (!user.phoneNumber.trim()) errors.phoneNumber = "Phone number is required";
    if (!user.postalCode.trim()) errors.postalCode = "Postal code is required";
    if (!user.country) errors.country = "Country is required";
    if (!user.state) errors.state = "State is required";
    if (!user.city) errors.city = "City is required";
    if (!user.address.trim()) errors.address = "Address is required";

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validate()) return;

    setHasEdited(true);
    dispatch(
      editUser({
        id: user.id,
        data: {
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          phoneNumber: user.phoneNumber,
          dateOfBirth: user.dateOfBirth,
          address: user.address,
          city: user.city,
          state: user.state,
          postalCode: user.postalCode,
          country: user.country,
          roleId: user.roleId,
        },
      })
    );
  };
  return (
    <Container className="w-100 p-2">
      <Card className={`shadow-lg rounded-4 ${styles.signupBody}`}>
        <Form className={styles.formBody} onSubmit={handleSubmit} noValidate>
          <Row className="g-2">
            {/* Username */}
            <Col md={6}>
              <Form.Group>
                <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
                  Username
                </Form.Label>
                <Form.Control type="text" value={user.userName} readOnly />
              </Form.Group>
            </Col>

            {/* Email */}
            <Col md={6}>
              <Form.Group>
                <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
                  Email
                </Form.Label>
                <Form.Control
                  type="email"
                  name="email"
                  value={user.email}
                  onChange={handleChange}
                  isInvalid={!!formErrors.email}
                  readOnly
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.email}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>

            {/* First Name */}
            <Col md={6}>
              <Form.Group>
                <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
                  First Name
                </Form.Label>
                <Form.Control
                  type="text"
                  name="firstName"
                  value={user.firstName}
                  onChange={handleChange}
                  isInvalid={!!formErrors.firstName}
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.firstName}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>

            {/* Last Name */}
            <Col md={6}>
              <Form.Group>
                <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
                  Last Name
                </Form.Label>
                <Form.Control
                  type="text"
                  name="lastName"
                  value={user.lastName}
                  onChange={handleChange}
                  isInvalid={!!formErrors.lastName}
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.lastName}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>

            {/* Phone Number */}
            <Col md={6}>
              <Form.Group>
                <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
                  Phone Number
                </Form.Label>
                <Form.Control
                  type="text"
                  name="phoneNumber"
                  value={user.phoneNumber}
                  onChange={handleChange}
                  isInvalid={!!formErrors.phoneNumber}
                  readOnly
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.phoneNumber}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>

            {/* Date of Birth */}
            <Col md={6}>
              <Form.Group>
                <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
                  Date of Birth
                </Form.Label>
                <Form.Control
                  type="date"
                  name="dateOfBirth"
                  value={user.dateOfBirth}
                  onChange={handleChange}
                />
              </Form.Group>
            </Col>

            {/* Role */}
            <Col md={6}>
              <Form.Group>
                <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
                  Role
                </Form.Label>
                <Form.Select
                  name="roleId"
                  value={user.roleId || ""}
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
            </Col>

            {/* Country */}
            <Col md={4}>
              <Form.Group>
                <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
                  Country
                </Form.Label>
                <Form.Select
                  name="country"
                  value={user.country}
                  onChange={handleChange}
                  isInvalid={!!formErrors.country}
                >
                  <option value="">-- Select Country --</option>
                  {countries.map((c) => (
                    <option key={c.id} value={c.name}>
                      {c.name}
                    </option>
                  ))}
                </Form.Select>
                <Form.Control.Feedback type="invalid">
                  {formErrors.country}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>

            {/* State */}
            <Col md={4}>
              <Form.Group>
                <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
                  State
                </Form.Label>
                <Form.Select
                  name="state"
                  value={user.state}
                  onChange={handleChange}
                  disabled={!user.country || states.length === 0}
                  isInvalid={!!formErrors.state}
                >
                  <option value="">-- Select State --</option>
                  {states.map((s) => (
                    <option key={s.id} value={s.name}>
                      {s.name}
                    </option>
                  ))}
                </Form.Select>
                <Form.Control.Feedback type="invalid">
                  {formErrors.state}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>

            {/* City */}
            <Col md={4}>
              <Form.Group>
                <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
                  City
                </Form.Label>
                <Form.Select
                  name="city"
                  value={user.city}
                  onChange={handleChange}
                  disabled={!user.state || cities.length === 0}
                  isInvalid={!!formErrors.city}
                >
                  <option value="">-- Select City --</option>
                  {cities.map((c) => (
                    <option key={c.id} value={c.name}>
                      {c.name}
                    </option>
                  ))}
                </Form.Select>
                <Form.Control.Feedback type="invalid">
                  {formErrors.city}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>

            {/* Postal Code */}
            <Col md={6}>
              <Form.Group>
                <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
                  Postal Code
                </Form.Label>
                <Form.Control
                  type="text"
                  name="postalCode"
                  value={user.postalCode}
                  onChange={handleChange}
                  isInvalid={!!formErrors.postalCode}
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.postalCode}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>

            {/* Address */}
            <Col md={6}>
              <Form.Group>
                <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
                  Address
                </Form.Label>
                <Form.Control
                  as="textarea"
                  rows={1}
                  name="address"
                  value={user.address}
                  onChange={handleChange}
                  isInvalid={!!formErrors.address}
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.address}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>
          </Row>

          {/* Submit Button */}
          <div className={`mt-3 ${styles.submitButton}`}>
            <Button type="submit" variant="success" disabled={editLoading}>
              {editLoading ? "Saving..." : "Update"}
            </Button>
          </div>
        </Form>
      </Card>
    </Container>
  );
};

EditUser.propTypes = {
  id: PropTypes.string,
  setShowModal: PropTypes.func,
}

export default EditUser;