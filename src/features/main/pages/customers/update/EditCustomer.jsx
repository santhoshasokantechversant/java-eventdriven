import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { Form, Button, Row, Col, Container, Card } from "react-bootstrap";
import {
  fetchCountriesForRegion,
  fetchStatesForCountry,
  fetchCitiesForState,
  clearStates,
  clearCities,
} from "../../../../../redux/slices/locationSlice";
import {
  editCustomer,
  fetchAllCustomers,
} from "../../../../../redux/slices/customerSlice";
import PropTypes from "prop-types";

export const EditCustomer = ({ customer, setShowModal }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  // Fetch location data from Redux store
  const { countries, states, cities } = useSelector((state) => state.locations);

  // Customer loading state
  const { loading } = useSelector((state) => state.customers);

  // Form state
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    country: "",
    state: "",
    city: "",
    postalCode: "",
    status: "",
    dateOfBirth: "",
    address: "",
  });

  // Validation error state
  const [formErrors, setFormErrors] = useState({});

  // Initialize with customer data
  useEffect(() => {
    if (customer) setFormData(customer);
  }, [customer]);

  // Load countries
  useEffect(() => {
    dispatch(fetchCountriesForRegion());
  }, [dispatch]);

  // Load states when country changes
  useEffect(() => {
    if (formData.country) {
      const selectedCountry = countries.find(
        (c) => c.name === formData.country
      );
      if (selectedCountry?.id)
        dispatch(fetchStatesForCountry(selectedCountry.id));
    } else {
      dispatch(clearStates());
      dispatch(clearCities());
    }
  }, [formData.country, countries, dispatch]);

  // Load cities when state changes
  useEffect(() => {
    if (formData.state) {
      const selectedState = states.find((s) => s.name === formData.state);
      if (selectedState?.id) dispatch(fetchCitiesForState(selectedState.id));
    } else {
      dispatch(clearCities());
    }
  }, [formData.state, states, dispatch]);

  // Update form values
  const handleChange = (e) => {
    const { name, value } = e.target;
    // Reset dependent dropdowns
    if (name === "country")
      setFormData({ ...formData, country: value, state: "", city: "" });
    else if (name === "state")
      setFormData({ ...formData, state: value, city: "" });
    else setFormData({ ...formData, [name]: value });
    // Clear validation message
    setFormErrors((prev) => ({ ...prev, [name]: "" }));
  };

  // Form validation rules
  const validate = () => {
    const errors = {};
    if (!/^[A-Za-z\s]{3,100}$/.test(formData.firstName || "")) {
      errors.firstName =
        "First name must contain only letters and spaces (3-100 characters)";
    }
    if (!/^[A-Za-z\s]{3,100}$/.test(formData.lastName || "")) {
      errors.lastName =
        "Last name must contain only letters and spaces (3-100 characters)";
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email || "")) {
      errors.email = "Enter a valid email address";
    }
    if (!/^\d{10}$/.test(formData.phoneNumber || "")) {
      errors.phoneNumber = "Phone Number must be exactly 10 digits";
    }
    if (!formData.status) errors.status = "Status is required";

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Submit updated customer data
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      const response = await dispatch(editCustomer(formData)).unwrap();
      setShowModal(false);
      toast.success(response.message);
      // Refresh customer list
      dispatch(fetchAllCustomers({ page: 0, size: 10 }));
      // Redirect back to list page
      setTimeout(() => navigate("/view-all-customer"), 1000);
    } catch (err) {
      toast.error(`Failed to update customer: ${err.message || err}`);
    }
  };

  return (
    <Container className="my-2">
      <Card className="p-2 shadow-sm">
        <Form onSubmit={handleSubmit} noValidate>
          <Row className="g-2 text-black">
            {/* First Name */}
            <Col md={6}>
              <Form.Group>
                <Form.Label>First Name</Form.Label>
                <Form.Control
                  type="text"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  isInvalid={!!formErrors.firstName}
                  placeholder="Enter First Name"
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.firstName}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>
            {/* Last Name */}
            <Col md={6}>
              <Form.Group>
                <Form.Label>Last Name</Form.Label>
                <Form.Control
                  type="text"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  isInvalid={!!formErrors.lastName}
                  placeholder="Enter Last Name"
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.lastName}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>
            {/* Email */}
            <Col md={6}>
              <Form.Group>
                <Form.Label>Email</Form.Label>
                <Form.Control
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  isInvalid={!!formErrors.email}
                  placeholder="Enter Email"
                  readOnly
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.email}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>
            {/* Phone Number */}
            <Col md={6}>
              <Form.Group>
                <Form.Label>Phone Number</Form.Label>
                <Form.Control
                  type="tel"
                  name="phoneNumber"
                  value={formData.phoneNumber}
                  onChange={handleChange}
                  isInvalid={!!formErrors.phoneNumber}
                  placeholder="Enter Phone Number"
                  readOnly
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.phoneNumber}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>
            {/* Country */}
            <Col md={6}>
              <Form.Group>
                <Form.Label>Country</Form.Label>
                <Form.Select
                  name="country"
                  value={formData.country}
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
            <Col md={6}>
              <Form.Group>
                <Form.Label>State</Form.Label>
                <Form.Select
                  name="state"
                  value={formData.state}
                  onChange={handleChange}
                  disabled={!formData.country || states.length === 0}
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
            <Col md={6}>
              <Form.Group>
                <Form.Label>City</Form.Label>
                <Form.Select
                  name="city"
                  value={formData.city}
                  onChange={handleChange}
                  disabled={!formData.state || cities.length === 0}
                  isInvalid={!!formErrors.city}
                >
                  <option value="">-- Select City --</option>
                  {cities.map((city) => (
                    <option key={city.id} value={city.name}>
                      {city.name}
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
                <Form.Label>Postal Code</Form.Label>
                <Form.Control
                  type="text"
                  name="postalCode"
                  value={formData.postalCode}
                  onChange={handleChange}
                />
              </Form.Group>
            </Col>
            {/* Status */}
            <Col md={6}>
              <Form.Group>
                <Form.Label>Status</Form.Label>
                <Form.Control
                  as="select"
                  name="status"
                  value={formData.status}
                  onChange={handleChange}
                  isInvalid={!!formErrors.status}
                  required
                >
                  <option value="">-- Select Status --</option>
                  <option value="ACTIVE">Active</option>
                  <option value="INACTIVE">Inactive</option>
                </Form.Control>
                <Form.Control.Feedback type="invalid">
                  {formErrors.status}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>
            {/* Date of Birth */}
            <Col md={6}>
              <Form.Group>
                <Form.Label>Date of Birth</Form.Label>
                <Form.Control
                  type="text"
                  value={formData.dateOfBirth}
                  onChange={handleChange}
                />
              </Form.Group>
            </Col>
            {/* Address */}
            <Col md={6}>
              <Form.Group>
                <Form.Label>Address</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={2}
                  name="address"
                  value={formData.address}
                  onChange={handleChange}
                />
              </Form.Group>
            </Col>
          </Row>

          {/* Buttons */}
          <div className="mt-3 d-flex justify-content-end gap-2">
            <Button type="submit" variant="primary" disabled={loading}>
              {loading ? "Updating..." : "Update"}
            </Button>
            <Button
              type="button"
              variant="danger"
              disabled={loading}
              onClick={() => setShowModal(false)}
            >
              Cancel
            </Button>
          </div>
        </Form>
      </Card>
    </Container>
  );
};

//Add prop types validation
EditCustomer.propTypes = {
  customer: PropTypes.shape({
    firstName: PropTypes.string,
    lastName: PropTypes.string,
    email: PropTypes.string,
    phoneNumber: PropTypes.string,
    country: PropTypes.string,
    state: PropTypes.string,
    city: PropTypes.string,
    postalCode: PropTypes.string,
    status: PropTypes.string,
    dateOfBirth: PropTypes.string,
    address: PropTypes.string,
  }),
  setShowModal: PropTypes.func,
};

export default EditCustomer;
