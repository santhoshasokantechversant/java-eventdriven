import axios from "axios";
import { showRefreshTokenModal } from "../utils/showRefreshTokenModal";

const apiUrl = import.meta.env.VITE_BACKEND_URL;
export const baseUrlEndPoint = axios.create({
  baseURL: `${apiUrl}/api/v2/endpoints`,
});

// create new endpoints
// Sends POST request with JWT token in headers.
export const addEndpoints = async (data) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlEndPoint.post(`/create`, data, {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
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

// fetch all endpoints
// Sends a GET request with the JWT token included in the request headers.
export const listAllEndpoints = async (data) => {
  try {
    const token = sessionStorage.getItem("access_token");

    const response = await baseUrlEndPoint.get(`/filter-list`, {
      params: data,
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
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

// edit endpoint by id
// Sends PUT request with JWT token in headers.
export const editEndpoints = async (id, data) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlEndPoint.put(`/update/${id}`, data, {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
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

// fetch endpoint by id
// Sends a GET request with the JWT token included in the request headers.
export const listEndpointsById = async (id) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlEndPoint.get(`/${id}`, {
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

// delete endpoint by id soft deletion
// Sends a DELETE request with the JWT token included in the request headers.
export const deleteEndpoints = async (id) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlEndPoint.delete(`/delete/${id}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
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
