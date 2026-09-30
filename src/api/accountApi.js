import axios from "axios";
import { showRefreshTokenModal } from "../utils/showRefreshTokenModal";

const apiUrl = import.meta.env.VITE_BACKEND_URL;

const API_BASE_URL = `${apiUrl}/api/v1/account`;

// fetches all accounts
// Sends POST request with JWT token in headers.
export const fetchAllAccounts = async (filter) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await axios.post(
      `${API_BASE_URL}/filter-accounts`,
      filter,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );
    return response.data.data;
  } catch (error) {
    if (error?.message?.toLowerCase() === "network error") {
      showRefreshTokenModal();
    } else {
      throw error;
    }
  }
};

// Fetches all account types for the dropdown.
// Sends a GET request with the JWT token included in the request headers.
export const fetchAccountType = async () => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await axios.get(`${API_BASE_URL}/account-types`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data.data;
  } catch (error) {
   if (error?.message?.toLowerCase() === "network error") {
      showRefreshTokenModal();
    } else {
      throw error;
    }
  }
};

// fetch currency for dropdown purpose
// Sends a GET request with the JWT token included in the request headers.
export const fetchCurrency = async () => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await axios.get(`${API_BASE_URL}/fetch-all-currency`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data.data;
  } catch (error) {
   if (error?.message?.toLowerCase() === "network error") {
      showRefreshTokenModal();
    } else {
      throw error;
    }
  }
};

// delete account by id soft deletion
// Sends a delete request with the JWT token included in the request headers.
export const deleteAccount = async (id) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await axios.delete(
      `${API_BASE_URL}/delete-account/${id}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
    return response.data.data;
  } catch (error) {
   if (error?.message?.toLowerCase() === "network error") {
      showRefreshTokenModal();
    } else {
      throw error;
    }
  }
};

// edit account by id 
// Sends a PUT request with the JWT token included in the request headers.
export const editAccountById = async (id, account) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await axios.put(
      `${API_BASE_URL}/update-account/${id}`,
      account,
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      }
    );
    return response.data;
  } catch (error) {
    if (error?.message?.toLowerCase() === "network error") {
      showRefreshTokenModal();
    } else {
      throw error;
    }
  }
};

// fetch account by id
// Sends a GET request with the JWT token included in the request headers.
export const fetchAccountById = async (id) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await axios.get(`${API_BASE_URL}/accounts/${id}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data.data;
  } catch (error) {
   if (error?.message?.toLowerCase() === "network error") {
      showRefreshTokenModal();
    } else {
      throw error;
    }
  }
};

// fetch account by customer id
// Sends a GET request with the JWT token included in the request headers.
export const fetchAccountByCustomerId = async (id) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await axios.get(
      `${API_BASE_URL}/get-account-by-customer-id-details/${id}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
    return response.data.data;
  } catch (error) {
   if (error?.message?.toLowerCase() === "network error") {
      showRefreshTokenModal();
    } else {
      throw error;
    }
  }
};

// fetch account status
// Sends a GET request with the JWT token included in the request headers.
export const fetchAccountStatus = async () => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await axios.get(`${API_BASE_URL}/account-status`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data.data;
  } catch (error) {
  if (error?.message?.toLowerCase() === "network error") {
      showRefreshTokenModal();
    } else {
      throw error;
    }
  }
};
