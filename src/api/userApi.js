import axios from "axios";
import { showRefreshTokenModal } from "../utils/showRefreshTokenModal";

const apiUrl = import.meta.env.VITE_BACKEND_URL;

export const baseUrlUsers = axios.create({
  baseURL: `${apiUrl}/api/v1/users`,
});

export const baseUrlRoles = axios.create({
  baseURL: `${apiUrl}/api/v1/roles`,
});

const getToken = () => sessionStorage.getItem("access_token");

export const login = async (loginData) => {
  const response = await baseUrlUsers.post("/login", loginData);
  return response.data;
};

// Logout function
// Sends a POST request to log the user out using the access token in headers
// and the refresh token as a request parameter.
export const logout = async () => {
  const token = sessionStorage.getItem("access_token");
  const refreshToken = sessionStorage.getItem("refresh_token");
  const response = await baseUrlUsers.post(`/logout`, null, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
    params: { refreshToken },
  });

  return response.data;
};

// fetch user by id
// Sends a GET request with the JWT token included in the request headers.
export const getUserById = async (id) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlUsers.get(`/get-user-by-id/${id}`, {
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

// edit user by id
// Sends PUT request with JWT token in headers.
export const editUser = async (id, data) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlUsers.put(`/update-user/${id}`, data, {
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

// fetch all users
// Sends a GET request with the JWT token included in the request headers.
export const fetchAllUsers = async (filter) => {
  try {
    const token = getToken();
    const params = {};
    Object.entries(filter).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        params[key] = value;
      }
    });
    const response = await baseUrlUsers.get(`/filter-users`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
      params,
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

// create new user
// Sends POST request with JWT token in headers.
export const addUser = async (user) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlUsers.post(`/add-user`, user, {
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

// delete user by id soft deletion
// Sends a DELETE request with the JWT token included in the request headers.
export const deleteUser = async (id) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlUsers.delete(`/delete-user/${id}`, {
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

// change password
// Sends PUT request with JWT token in headers.
export const changePassword = async (id, data) => {
  try {
    const token = sessionStorage.getItem("access_token");
    const response = await baseUrlUsers.put(
      `/update-new-password/${id}`,
      data,
      {
        headers: {
          Authorization: `Bearer ${token}`,
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
}
