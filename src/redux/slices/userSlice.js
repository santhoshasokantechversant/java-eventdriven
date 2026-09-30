import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import {
  getUserById,
  editUser as editUserApi,
  fetchAllUsers as fetchAllUsersApi,
  addUser,
  deleteUser as deleteUserApi,
  changePassword,
} from "../../api/userApi";

/* -------------------------------------------------------------------------- */
/*                          ASYNC THUNKS – API CALLS                          */
/* -------------------------------------------------------------------------- */

// fetch user by id
export const fetchUserById = createAsyncThunk(
  "user/fetchUserById",
  async (id, { rejectWithValue }) => {
    try {
      const response = await getUserById(id);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// edit/update user by id
export const editUser = createAsyncThunk(
  "user/editUser",
  async ({ id, data }, { rejectWithValue }) => {
    try {
      const response = await editUserApi(id, data);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// fetch all users
export const fetchUsers = createAsyncThunk(
  "user/fetchUsers",
  async (filter, { rejectWithValue }) => {
    try {
      const response = await fetchAllUsersApi(filter);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// create new user
export const createUser = createAsyncThunk(
  "user/createUser",
  async (userData, { rejectWithValue }) => {
    try {
      const response = await addUser(userData);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// delete user by id soft deletion
export const deleteUser = createAsyncThunk(
  "user/deleteUser",
  async (id, { rejectWithValue }) => {
    try {
      const response = await deleteUserApi(id);
      return { id, response };
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// Update new password
export const updateNewPassword = createAsyncThunk(
  "user/changePassword",
  async ({ id, data }, { rejectWithValue }) => {
    try {
      const response = await changePassword(id, data);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// Slice definition
const userSlice = createSlice({
  name: "user",
  initialState: {
    userDetails: null,
    users: {
      currentPage: 0,
      totalPages: 0,
      totalItems: 0,
      pageSize: 10,
      data: [],
    },
    loading: false,
    error: null,
    editLoading: false,
    editError: null,
    usersLoading: false,
    usersError: null,
    createLoading: false,
    createError: null,
    createSuccess: false,
    createResponse: null,
    deleteLoading: false,
    deleteError: null,
    deleteSuccess: false,
    editResponse: null,
  },
  reducers: {
    clearUserDetails(state) {
      state.userDetails = null;
      state.error = null;
      state.editError = null;
    },
    clearUsers(state) {
      state.users = [];
      state.usersError = null;
      state.pagination = {
        currentPage: 0,
        totalPages: 0,
        totalItems: 0,
        pageSize: 10,
      };
    },
    clearCreateUserStatus(state) {
      state.createLoading = false;
      state.createError = null;
      state.createSuccess = false;
    },
    clearDeleteUserStatus(state) {
      state.deleteLoading = false;
      state.deleteError = null;
      state.deleteSuccess = false;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch single user
      .addCase(fetchUserById.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUserById.fulfilled, (state, action) => {
        state.loading = false;
        state.userDetails = action.payload;
      })
      .addCase(fetchUserById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Edit user
      .addCase(editUser.pending, (state) => {
        state.editLoading = true;
        state.editError = null;
      })
      .addCase(editUser.fulfilled, (state, action) => {
        state.editLoading = false;
        state.userDetails = action.payload;
        state.editResponse = action.payload;
      })
      .addCase(editUser.rejected, (state, action) => {
        state.editLoading = false;
        state.editError = action.payload;
      })

      // Fetch all users
      .addCase(fetchUsers.pending, (state) => {
        state.usersLoading = true;
        state.usersError = null;
      })
      .addCase(fetchUsers.fulfilled, (state, action) => {
        state.usersLoading = false;
        state.users = action.payload;
      })
      .addCase(fetchUsers.rejected, (state, action) => {
        state.usersLoading = false;
        state.usersError = action.payload;
      })

      // Create user
      .addCase(createUser.pending, (state) => {
        state.createLoading = true;
        state.createError = null;
        state.createSuccess = false;
      })
      .addCase(createUser.fulfilled, (state, action) => {
        state.createLoading = false;
        state.createSuccess = true;
        state.createResponse = action.payload;
      })
      .addCase(createUser.rejected, (state, action) => {
        state.createLoading = false;
        state.createError = action.payload;
        state.createSuccess = false;
      })
      // delete user
      .addCase(deleteUser.pending, (state) => {
        state.deleteLoading = true;
        state.deleteError = null;
        state.deleteSuccess = false;
      })
      .addCase(deleteUser.fulfilled, (state, action) => {
        state.deleteLoading = false;
        state.deleteSuccess = action.payload.response;
      })
      .addCase(deleteUser.rejected, (state, action) => {
        state.deleteLoading = false;
        state.deleteError = action.payload;
        state.deleteSuccess = false;
      })

      // Update new password
      .addCase(updateNewPassword.pending, (state) => {
        state.editLoading = true;
        state.editError = null;
      })
      .addCase(updateNewPassword.fulfilled, (state, action) => {
        state.editLoading = false;
        state.userDetails = action.payload;
        state.editResponse = action.payload;
      })
      .addCase(updateNewPassword.rejected, (state, action) => {
        state.editLoading = false;
        state.editError = action.payload;
      });
  },
});

export const {
  clearUserDetails,
  clearUsers,
  clearCreateUserStatus,
  clearDeleteUserStatus,
} = userSlice.actions;

export default userSlice.reducer;
