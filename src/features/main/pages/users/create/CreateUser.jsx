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
  clearCreateUserStatus,
  createUser,
  fetchUsers,
} from "../../../../../redux/slices/userSlice";
import styles from "../update/EditUser.module.css";

export const CreateUser = ({ setShowModal }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const { countries, states, cities } = useSelector((state) => state.locations);
  const roleList = useSelector((state) => state.role.roles.roleList || []);

  const { createLoading, createError, createSuccess, createResponse } = useSelector(
    (state) => state.user
  );

  const [formData, setFormData] = useState({
    email: "",
    firstName: "",
    lastName: "",
    roleId: "",
    phoneNumber: "",
    dateOfBirth: "",
    address: "",
    city: "",
    state: "",
    postalCode: "",
    country: "",
  });

  const [formErrors, setFormErrors] = useState({});

  // Load countries and roles on mount
  useEffect(() => {
    dispatch(fetchCountriesForRegion());
    dispatch(fetchAllRolesDropdown());
  }, [dispatch]);

  // Load states when country changes
  useEffect(() => {
    if (formData.country) {
      const selectedCountry = countries.find(
        (c) => c.name === formData.country
      );
      if (selectedCountry?.id)
        dispatch(fetchStatesForCountry(selectedCountry.id));
      setFormData((prev) => ({ ...prev, state: "", city: "" }));
    }
  }, [formData.country, countries, dispatch]);

  // Load cities when state changes
  useEffect(() => {
    if (formData.state) {
      const selectedState = states.find((s) => s.name === formData.state);
      if (selectedState?.id) dispatch(fetchCitiesForState(selectedState.id));
      setFormData((prev) => ({ ...prev, city: "" }));
    }
  }, [formData.state, states, dispatch]);

  // Handle success/error notifications and redirect
  useEffect(() => {
    if (createSuccess && createResponse) {
      setShowModal(false);
      toast.success(createResponse.message);
      dispatch(clearCreateUserStatus());
      dispatch(fetchUsers({ page: 0, size: 10 }, navigate));
      navigate("/view-all-users");
    }
    if (createError) {
      toast.error(`Failed to create user: ${createError.message}`);
      dispatch(clearCreateUserStatus());
    }
  }, [createSuccess, createError, dispatch, navigate]);

  // Handle input value changes
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  // form validation
  const validate = () => {
    const errors = {};

    if (!formData.firstName.trim()) {
      errors.firstName = "First name is required";
    } else if (!/^[A-Za-z\s]{3,50}$/.test(formData.firstName)) {
      errors.firstName = "First name must be 3–50 letters only";
    }

    if (!formData.lastName.trim()) {
      errors.lastName = "Last name is required";
    } else if (!/^[A-Za-z\s]{1,100}$/.test(formData.lastName)) {
      errors.lastName = "Last name must be 1–50 letters only";
    }

    if (!formData.email.trim()) {
      errors.email = "Email is required";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      errors.email = "Invalid email address";
    }

    if (!formData.phoneNumber.trim()) {
      errors.phoneNumber = "Phone number is required";
    } else if (!/^\d{10}$/.test(formData.phoneNumber)) {
      errors.phoneNumber = "Phone number must be exactly 10 digits";
    }

    if (!formData.postalCode.trim()) {
      errors.postalCode = "Postal code is required";
    } else if (!/^\d{6}$/.test(formData.postalCode)) {
      errors.postalCode = "Postal code must be exactly 6 digits";
    }

    if (!formData.roleId) errors.roleId = "Role is required";
    if (!formData.country) errors.country = "Country is required";
    if (!formData.state) errors.state = "State is required";
    if (!formData.city) errors.city = "City is required";
    if (!formData.address.trim()) errors.address = "Address is required";

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Handle form submission
  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validate()) return;
    dispatch(createUser(formData));
  };

  return (
    <Container className="w-100 p-3">
      <Card className={`shadow-lg rounded-4 ${styles.signupBody}`}>
        <Form className={styles.formBody} onSubmit={handleSubmit} noValidate>
          <Row className="g-3">
            {/* Email */}
            <Col md={6}>
              <Form.Group>
                <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
                  Email
                </Form.Label>
                <Form.Control
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="Enter Email"
                  isInvalid={!!formErrors.email}
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
                  value={formData.firstName}
                  onChange={handleChange}
                  placeholder="Enter First Name"
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
                  value={formData.lastName}
                  onChange={handleChange}
                  placeholder="Enter Last Name"
                  isInvalid={!!formErrors.lastName}
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.lastName}
                </Form.Control.Feedback>
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
                  value={formData.roleId}
                  onChange={handleChange}
                  disabled={!roleList.length}
                  isInvalid={!!formErrors.roleId}
                >
                  <option value="">-- Select Role --</option>
                  {roleList
                    .filter((role) => role.name !== "Customer") // exclude "Customer"
                    .map((role) => (
                      <option key={role.id} value={role.id}>
                        {role.name}
                      </option>
                    ))}
                </Form.Select>
                <Form.Control.Feedback type="invalid">
                  {formErrors.roleId}
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
                  value={formData.phoneNumber}
                  onChange={handleChange}
                  placeholder="Enter Phone Number"
                  isInvalid={!!formErrors.phoneNumber}
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
                  value={formData.dateOfBirth}
                  onChange={handleChange}
                />
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
                  value={formData.country}
                  onChange={handleChange}
                  isInvalid={!!formErrors.country}
                >
                  <option value="">-- Select Country --</option>
                  {countries?.map((c) => (
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
                  value={formData.state}
                  onChange={handleChange}
                  disabled={!formData.country || states.length === 0}
                  isInvalid={!!formErrors.state}
                >
                  <option value="">-- Select State --</option>
                  {states?.map((s) => (
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
                  value={formData.city}
                  onChange={handleChange}
                  disabled={!formData.state || cities.length === 0}
                  isInvalid={!!formErrors.city}
                >
                  <option value="">-- Select City --</option>
                  {cities?.map((c) => (
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
                  value={formData.postalCode}
                  onChange={handleChange}
                  placeholder="Enter Postal Code"
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
                  rows={2}
                  name="address"
                  value={formData.address}
                  onChange={handleChange}
                  placeholder="Enter Address"
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
            <Button type="submit" variant="success" disabled={createLoading}>
              {createLoading ? "Saving..." : "Save"}
            </Button>
          </div>
        </Form>
      </Card>
    </Container>
  );
};

// Add prop types validation
CreateUser.propTypes = {
  setShowModal: PropTypes.func,
}

export default CreateUser;
