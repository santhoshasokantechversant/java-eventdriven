import axios from "axios";
import { showRefreshTokenModal } from "../utils/showRefreshTokenModal";
const apiUrl = import.meta.env.VITE_BACKEND_URL;
export const baseUrlRoles = axios.create({
  baseURL: `${apiUrl}/api/v1/roles`,
});

// create new roles
// Sends POST request with JWT token in headers.
export const addRole = async (data) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlRoles.post(`/add`, data, {
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

// fetch all roles
// Sends a GET request with the JWT token included in the request headers.
export const fetchAllRole = async (params = {}) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlRoles.get("/filter-roles", {
      params,
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

// delete role by id soft deletion
// Sends a DELETE request with the JWT token included in the request headers.
export const deleteRole = async (id) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlRoles.delete(`/delete-role/${id}`, {
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

// edit role by id
// Sends PUT request with JWT token in headers.
export const editRole = async (id, data) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlRoles.put(`/update-role/${id}`, data, {
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

// fetch role by id
// Sends a GET request with the JWT token included in the request headers.
export const fetchRoleById = async (id) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlRoles.get(`/get-role-by-id/${id}`, {
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

// fetch all roles with pagination 
// Sends a GET request with the JWT token included in the request headers.
export const fetchRoles = async () => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlRoles.get(`/get-all-roles`, {
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

// fetch all roles without pagination 
// Sends a GET request with the JWT token included in the request headers.
export const fetchAllRoles = async () => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlRoles.get(`/fetch-all-roles`, {
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

// fetch roles for dropdown
// Sends a GET request with the JWT token included in the request headers.
export const fetchRolesDropdown = async () => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlRoles.get(`/dropdown-list`, {
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
}