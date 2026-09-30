import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import {
  addRole,
  deleteRole,
  editRole,
  fetchAllRole,
  fetchRoleById,
  fetchRoles,
  fetchRolesDropdown,
} from "../../api/roleApi";
import { fetchAllRoles as fetchAllRolesApi } from "../../api/roleApi";


/* -------------------------------------------------------------------------- */
/*                          ASYNC THUNKS – API CALLS                          */
/* -------------------------------------------------------------------------- */

// add new role
export const addRoles = createAsyncThunk(
  "role/addRole",
  async (data, { rejectWithValue }) => {
    try {
      const response = await addRole(data);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// fetch all roles
export const getAllRoles = createAsyncThunk(
  "role/fetchAllRole",
  async (params = {}, { rejectWithValue }) => {
    try {
      const response = await fetchAllRole(params);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// delete role by id soft deletion
export const deleteRoleById = createAsyncThunk(
  "role/deleteRole",
  async (id, { rejectWithValue }) => {
    try {
      const response = await deleteRole(id);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// edit/update role by id
export const editRoleById = createAsyncThunk(
  "role/editRole",
  async ({ id, data }, { rejectWithValue }) => {
    try {
      const response = await editRole(id, data);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// fetch role by id
export const getRoleById = createAsyncThunk(
  "role/fetchRoleById",
  async (id, { rejectWithValue }) => {
    try {
      const response = await fetchRoleById(id);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// fetch all roles with pagination 
export const getRoles = createAsyncThunk(
  "role/getRoles",
  async (_, { rejectWithValue }) => {
    try {
      const response = await fetchRoles();
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// fetch all roles without pagination 
export const fetchAllRoles = createAsyncThunk(
  "role/fetchAllRoles",
  async (_, { rejectWithValue }) => {
    try {
      const response = await fetchAllRolesApi();
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// fetch all roles for dropdown
export const fetchAllRolesDropdown = createAsyncThunk(
  "role/fetchRolesDropdown",
  async (_, { rejectWithValue }) => {
    try {
      const response = await fetchRolesDropdown();
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// slice definition
const roleSlice = createSlice({
  name: "role",
  initialState: {
    roles: {
      roleList: [],
      currentPage: 0,
      pageSize: 10,
      totalItems: 0,
      totalPages: 0,
    },
    roleDetails: null,
    addLoading: false,
  },
  reducers: {
    clearRoleDetails(state) {
      state.roleDetails = null;
      state.error = null;
      state.addError = null;
    },
  },
  extraReducers: (builder) => {
    builder

      // add roles
      .addCase(addRoles.pending, (state) => {
        state.addLoading = true;
        state.addError = null;
      })
      .addCase(addRoles.fulfilled, (state, action) => {
        state.addLoading = false;
        state.roleDetails = action.payload;
      })
      .addCase(addRoles.rejected, (state, action) => {
        state.addLoading = false;
        state.addError = action.payload;
      })

      // fetch all roles using filter api.
      .addCase(getAllRoles.pending, (state) => {
        state.error = null;
        state.addLoading = true;
      })
      .addCase(getAllRoles.fulfilled, (state, action) => {
        state.roles = action.payload;
        state.addLoading = false;
      })
      .addCase(getAllRoles.rejected, (state, action) => {
        state.addLoading = false;
        state.addError = action.payload;
      })

      //delete roles by id
      .addCase(deleteRoleById.pending, (state) => {
        state.error = null;
      })
      .addCase(deleteRoleById.fulfilled, (state, action) => {
        state.id = action.payload;
      })
      .addCase(deleteRoleById.rejected, (state, action) => {
        state.error = action.payload;
      })

      //update roles by id
      .addCase(editRoleById.pending, (state) => {
        state.addLoading = true;
        state.error = null;
      })
      .addCase(editRoleById.fulfilled, (state, action) => {
        state.id = action.payload;
      })
      .addCase(editRoleById.rejected, (state, action) => {
        state.error = action.payload;
      })

      //get role by id
      .addCase(getRoleById.pending, (state) => {
        state.error = null;
      })
      .addCase(getRoleById.fulfilled, (state, action) => {
        state.currentAccount = action.payload;
      })
      .addCase(getRoleById.rejected, (state, action) => {
        state.error = action.payload;
      })

      // fetch all roles
      .addCase(fetchAllRoles.pending, (state) => {
        state.error = null;
        state.addLoading = true;
      })
      .addCase(fetchAllRoles.fulfilled, (state, action) => {
        state.roles.roleList = action.payload || [];
        state.addLoading = false;
      })
      .addCase(fetchAllRoles.rejected, (state, action) => {
        state.addLoading = false;
        state.addError = action.payload;
      })
      
      // fetch all roles for dropdown
      .addCase(fetchAllRolesDropdown.pending, (state) => {
        state.error = null;
        state.addLoading = true;
      })
      .addCase(fetchAllRolesDropdown.fulfilled, (state, action) => {
        state.roles.roleList = action.payload || [];
        state.addLoading = false;
      })
      .addCase(fetchAllRolesDropdown.rejected, (state, action) => {
        state.addLoading = false;
        state.addError = action.payload;
      });
  },
});

export default roleSlice.reducer;
