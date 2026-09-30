import axios from "axios";
import { showRefreshTokenModal } from "../utils/showRefreshTokenModal";
const apiUrl = import.meta.env.VITE_BACKEND_URL;

const API_BASE_URL = `${apiUrl}/api/v1/customers/locations`;

const getToken = () => sessionStorage.getItem("access_token");

// fetch countries for region
// Sends a GET request with the JWT token included in the request headers.
export const getCountriesForRegion = async () => {
  try {
    const token = getToken();
    const response = await axios.get(
      `${API_BASE_URL}/get-countries-for-region`,
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

// fetch states for country 
// Sends a GET request with the JWT token included in the request headers.
export const getStatesForCountry = async (id) => {
  try {
    const token = getToken();
    const response = await axios.get(
      `${API_BASE_URL}/get-states-for-country/${id}`,
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

// fetch cities for state
// Sends a GET request with the JWT token included in the request headers.
export const getCityForState = async (id) => {
  try {
    const token = getToken();
    const response = await axios.get(
      `${API_BASE_URL}/get-city-for-state/${id}`,
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
