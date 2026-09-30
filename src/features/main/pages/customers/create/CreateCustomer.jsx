import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { Form, Button, Row, Col, Container, Card } from "react-bootstrap";
import {
  fetchCountriesForRegion,
  fetchStatesForCountry,
  fetchCitiesForState,
} from "../../../../../redux/slices/locationSlice";
import {
  createNewCustomer,
  fetchAllCustomers,
} from "../../../../../redux/slices/customerSlice";
import PropTypes from "prop-types";

export const CreateCustomer = ({ setShowModal }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  //Extract location lists from Redux store
  const {
    countries,
    states,
    cities,
  } = useSelector((state) => state.locations);

  //Customer creation loading state
  const { loading: creating } = useSelector((state) => state.customers);

  // Main form data
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    country: "",
    state: "",
    city: "",
    postalCode: "",
    address: "",
    dateOfBirth: "",
  });

  const [formErrors, setFormErrors] = useState({});

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
      if (selectedCountry?.id) {
        dispatch(fetchStatesForCountry(selectedCountry.id));
      }
      setFormData((prev) => ({ ...prev, state: "", city: "" }));
    }
  }, [formData.country, countries, dispatch]);

  // Load cities when state changes
  useEffect(() => {
    if (formData.state) {
      const selectedState = states.find((s) => s.name === formData.state);
      if (selectedState?.id) {
        dispatch(fetchCitiesForState(selectedState.id));
      }
      // Reset city when state changes
      setFormData((prev) => ({ ...prev, city: "" }));
    }
  }, [formData.state, states, dispatch]);

  //Handle input changes and reset dependent fields
  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData((prev) => {
      if (name === "country") {
        return { ...prev, country: value, state: "", city: "" };
      } else if (name === "state") {
        return { ...prev, state: value, city: "" };
      } else {
        return { ...prev, [name]: value };
      }
    });
  };

  //Validate all form fields before submitting
  const validate = () => {
    const errors = {};

    if (!/^[A-Za-z\s]{3,100}$/.test(formData.firstName || "")) {
      errors.firstName =
        "First name must contain only letters and spaces (3-100 characters)";
    }
    if (!/^[A-Za-z\s]{1,100}$/.test(formData.lastName || "")) {
      errors.lastName =
        "Last name must contain only letters and spaces (1-50 characters)";
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email || "")) {
      errors.email = "Enter a valid email address";
    }
    if (!/^\d{10}$/.test(formData.phoneNumber || "")) {
      errors.phoneNumber = "Phone Number must be exactly 10 digits";
    }
    if (!formData.address || formData.address.length > 250) {
      errors.address = "Address cannot exceed 250 characters";
    }
    if (!/^\d{6}$/.test(formData.postalCode || "")) {
      errors.postalCode = "Postal Code must be exactly 6 digits";
    }
    if (!formData.country) errors.country = "Country is required";
    if (!formData.state) errors.state = "State is required";
    if (!formData.city) errors.city = "City is required";

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  //Submit form data to backend
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    try {
      await dispatch(createNewCustomer(formData))
        .unwrap()
        .then(() => {
          dispatch(fetchAllCustomers({}));
          setShowModal(false);
        });
      toast.success("Customer created successfully!");
      setTimeout(() => {
        navigate("/view-all-customer");
      }, 1000);
    } catch (error) {
      toast.error(`Failed to create customer: ${error}`);
    }
  };

  return (
    <Container className="my-2">
      <Card className="p-2 shadow-sm">
        <Form noValidate onSubmit={handleSubmit}>
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
                  disabled={creating}
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
                  disabled={creating}
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
                  disabled={creating}
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
                  disabled={creating}
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
                  disabled={creating}
                >
                  <option value="">-- Select --</option>
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
                  isInvalid={!!formErrors.state}
                  disabled={!formData.country || creating}
                >
                  <option value="">-- Select --</option>
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
                  isInvalid={!!formErrors.city}
                  disabled={!formData.state || creating}
                >
                  <option value="">-- Select --</option>
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
                <Form.Label>Postal Code</Form.Label>
                <Form.Control
                  type="text"
                  name="postalCode"
                  value={formData.postalCode}
                  onChange={handleChange}
                  isInvalid={!!formErrors.postalCode}
                  placeholder="Enter Postal Code"
                  disabled={creating}
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.postalCode}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>

            {/* Date of Birth */}
            <Col md={6}>
              <Form.Group>
                <Form.Label>Date of Birth</Form.Label>
                <Form.Control
                  type="date"
                  name="dateOfBirth"
                  value={formData.dateOfBirth}
                  onChange={handleChange}
                  disabled={creating}
                />
              </Form.Group>
            </Col>

            {/* Address */}
            <Col md={12}>
              <Form.Group>
                <Form.Label>Address</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={2}
                  name="address"
                  value={formData.address}
                  onChange={handleChange}
                  isInvalid={!!formErrors.address}
                  placeholder="Enter Address"
                  disabled={creating}
                />
                <Form.Control.Feedback type="invalid">
                  {formErrors.address}
                </Form.Control.Feedback>
              </Form.Group>
            </Col>
          </Row>

          <div className="mt-2 d-flex justify-content-end">
            <Button type="submit" variant="primary" disabled={creating}>
              {creating ? "Saving..." : "Save"}
            </Button>
          </div>
        </Form>
      </Card>
    </Container>
  );
};

CreateCustomer.propTypes = {
  setShowModal: PropTypes.func,
}

export default CreateCustomer;