import axios from "axios";
import { showRefreshTokenModal } from "../utils/showRefreshTokenModal";

const apiUrl = import.meta.env.VITE_BACKEND_URL;

const API_BASE_URL = `${apiUrl}/api/v1/customers`;

const getToken = () => sessionStorage.getItem("access_token");

// fetch all customers
// Sends a GET request with the JWT token included in the request headers.
export const fetchAllCustomers = async (filter) => {
  try {
    const token = getToken();
    const params = {};
    Object.entries(filter).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        params[key] = value;
      }
    });

    const response = await axios.get(`${API_BASE_URL}/view-all-customers`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
      params,
    });
    return response.data;
  } catch (error) {
    if (error?.message?.toLowerCase() === "network error") {
      showRefreshTokenModal();
    } else {
      throw error;
    }
  }
};

// delete customer by id soft deletion
// Sends a DELETE request with the JWT token included in the request headers.
export const deleteCustomerById = async (id) => {
  try {
    const token = getToken();
    const response = await axios.delete(
      `${API_BASE_URL}/delete-customer-by-id/${id}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
    return response;
  } catch (error) {
     if (error?.message?.toLowerCase() === "network error") {
      showRefreshTokenModal();
    } else {
      throw error;
    }
  }
};

// fetch customer by id
// Sends a GET request with the JWT token included in the request headers.
export const getCustomerById = async (id) => {
  try {
    const token = getToken();
    const response = await axios.get(
      `${API_BASE_URL}/get-customer-by-id/${id}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
    return response;
  } catch (error) {
     if (error?.message?.toLowerCase() === "network error") {
      showRefreshTokenModal();
    } else {
      throw error;
    }
  }
};

// create new customer
// Sends POST request with JWT token in headers.
export const createCustomer = async (customer) => {
  try {
    const token = getToken();
    const response = await axios.post(
      `${API_BASE_URL}/create-customer`,
      customer,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );
    return response;
  } catch (error) {
    if (error?.message?.toLowerCase() === "network error") {
      showRefreshTokenModal();
    } else {
      throw error;
    }
  }
};

// edit customer by id
// Sends PUT request with JWT token in headers.
export const editCustomer = async (customer) => {
  try {
    const token = getToken();
    const response = await axios.put(
      `${API_BASE_URL}/${customer.customerNo}`,
      customer,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );
    return response;
  } catch (error) {
     if (error?.message?.toLowerCase() === "network error") {
      showRefreshTokenModal();
    } else {
      throw error;
    }
  }
};
