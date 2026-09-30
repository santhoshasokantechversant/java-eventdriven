import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { login, logout as logoutApi } from "../../api/userApi";

/* -------------------------------------------------------------------------- */
/*                           ASYNC THUNKS – API CALLS                         */
/* -------------------------------------------------------------------------- */

/**
 * Login user
 * Sends login credentials to the backend and returns user details + tokens.
 */
export const loginUser = createAsyncThunk(
  "auth/loginUser",
  async (loginData, { rejectWithValue }) => {
    try {
      const response = await login(loginData);
      return response;
    } catch (error) {
      const errMsg = error?.response?.data || error?.message || "Logout failed";
      return rejectWithValue(errMsg);
    }
  }
);

/**
 * Logout user
 * Calls logout API and clears all session storage tokens & user data.
 */
export const logoutUser = createAsyncThunk(
  "auth/logoutUser",
  async (_, { rejectWithValue }) => {
    try {
      const response = await logoutApi();
      sessionStorage.removeItem("access_token");
      sessionStorage.removeItem("refresh_token");
      sessionStorage.removeItem("fullName");
      sessionStorage.removeItem("roleId");
      sessionStorage.removeItem("userId");
      sessionStorage.removeItem("sideNav");
      return response;
    } catch (error) {
      const errMsg = error?.response?.data || error?.message || "Logout failed";
      return rejectWithValue(errMsg);
    }
  }
);

// Slice definition
const authSlice = createSlice({
  name: "auth",
  initialState: {
    user: null,
    loading: false,
    error: null,
    isAuthenticated: false,
  },
  reducers: {
     /**
     * Clear all authentication data manually
     * Useful when expiring tokens or resetting state globally.
     */
    clearAuth(state) {
      state.user = null;
      state.isAuthenticated = false;
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // login
      .addCase(loginUser.pending, (state) => {
        state.loading = true;
        state.error = null;
        state.isAuthenticated = false;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload;
        state.isAuthenticated = true;
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
        state.isAuthenticated = false;
      })
      // logout
      .addCase(logoutUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(logoutUser.fulfilled, (state) => {
        state.loading = false;
        state.user = null;
        state.isAuthenticated = false;
        state.error = null;
      })
      .addCase(logoutUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export const { clearAuth } = authSlice.actions;

export default authSlice.reducer;
