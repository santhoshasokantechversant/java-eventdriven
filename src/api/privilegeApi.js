import axios from "axios";
import { showRefreshTokenModal } from "../utils/showRefreshTokenModal";

const apiUrl = import.meta.env.VITE_BACKEND_URL;
export const baseUrlRoles = axios.create({
  baseURL: `${apiUrl}/api/v1/privileges-permissions`,
});

export const baseUrlPrivileges = axios.create({
  baseURL: `${apiUrl}/api/v2/privileges`,
});

export const baseUrlPermission = axios.create({
  baseURL: `${apiUrl}/api/v2/permission`,
});

// create new privileges
// Sends POST request with JWT token in headers.
export const addPrivileges = async (data) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlPrivileges.post(`/create`, data, {
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

// fetch privileges by id
// Sends a GET request with the JWT token included in the request headers.
export const listPrivilegesId = async (id) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlPrivileges.get(`/${id}`, {
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

// fetch all privileges
// Sends a GET request with the JWT token included in the request headers.
export const listPrivileges = async (data) => {
  try {
    const token = sessionStorage.getItem("access_token");

    const response = await baseUrlPrivileges.get(`/filter-list`, {
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

// edit privileges by id
// Sends PUT request with JWT token in headers.
export const editPrivileges = async (id, data) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlPrivileges.put(`/update/${id}`, data, {
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

// delete privileges by id soft deletion
// Sends a DELETE request with the JWT token included in the request headers.
export const deletePrivileges = async (id) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlPrivileges.delete(`/delete/${id}`, {
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

// fetch privileges for dropdown
// Sends a GET request with the JWT token included in the request headers.
export const listPrivilegesDropdown = async (data) => {
  try {
    const token = sessionStorage.getItem("access_token");

    const response = await baseUrlPrivileges.get(`/list`, {
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

// fetch side nav
// Sends a GET request with the JWT token included in the request headers.
export const fetchSideNav = async () => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlRoles.get(`/list-side-nav`, {
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

// fetch permissions by roles
// Sends a GET request with the JWT token included in the request headers.
export const getPermissions = async (id) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlPermission.get(`/list-by-roles/${id}`, {
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

// edit permissions by id
// Sends a PUT request with the JWT token included in the request headers.
export const editPermissions = async (id, data) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlPermission.put(`/update/${id}`, data, {
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

// Saves or updates role permissions for a specific role.
// Sends a PUT request with the role ID and payload, including JWT authentication headers.
export const savePermissions = async (id, data) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlPermission.put(`/save-roles/${id}`, data, {
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

// fetch permissions by id
// Sends a GET request with the JWT token included in the request headers.
export const getPermissionsById = async (id) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlPermission.get(`/get-permission/${id}`, {
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
