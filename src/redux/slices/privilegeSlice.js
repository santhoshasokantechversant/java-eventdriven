import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import {
  addPrivileges,
  deletePrivileges,
  editPermissions,
  editPrivileges,
  fetchSideNav,
  getPermissions,
  getPermissionsById,
  listPrivileges,
  listPrivilegesId,
  savePermissions,
  listPrivilegesDropdown,
} from "../../api/privilegeApi";


/* -------------------------------------------------------------------------- */
/*                          ASYNC THUNKS – API CALLS                          */
/* -------------------------------------------------------------------------- */

 // add new privileges permissions
export const addPrivilegesPermissions = createAsyncThunk(
  "privileges/addPrivileges",
  async (data, { rejectWithValue }) => {
    try {
      const response = await addPrivileges(data);

      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

 // fetch privileges by role id
export const getPrivilegesByRoleId = createAsyncThunk(
  "privileges/listPrivilegesId",
  async (id, { rejectWithValue }) => {
    try {
      const response = await listPrivilegesId(id);
      return response;
    } catch (error) {
     return rejectWithValue(error.response?.data || error.message);
    }
  }
);

 // edit/update privileges by id
export const editPrivilegesById = createAsyncThunk(
  "privileges/editPrivileges",
  async ({ id, data }, { rejectWithValue }) => {
    try {
      const response = await editPrivileges(id, data);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

 // fetch side nav
export const getSideNav = createAsyncThunk(
  "privileges/fetchSideNav",
  async (_, { rejectWithValue }) => {
    try {
      const response = await fetchSideNav();
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

 // fetch permission by id
export const getPermissionById = createAsyncThunk(
  "privileges/getPermissions",
  async (id, { rejectWithValue }) => {
    try {
      const response = await getPermissions(id);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

 // edit permission by id
export const editPermissionsById = createAsyncThunk(
  "privileges/editPermissions",
  async ({ id, data }, { rejectWithValue }) => {
    try {
      const response = await editPermissions(id, data);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

 // fetch all privileges
export const fetchAllPrivileges = createAsyncThunk(
  "privileges/listPrivileges",
  async (data, { rejectWithValue }) => {
    try {
      const response = await listPrivileges(data);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

 // fetch privileges for dropdown
export const fetchPrivilegesDropdown = createAsyncThunk(
  "privileges/fetchPrivilegesDropdown",
  async (data, { rejectWithValue }) => {
    try {
      const response = await listPrivilegesDropdown();
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

 // delete privileges by id soft deletion
export const deletePrivilegesById = createAsyncThunk(
  "privileges/deletePrivileges",
  async (id, { rejectWithValue }) => {
    try {
      const response = await deletePrivileges(id);
      return { id, response };
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

 // save new permissions by role id
export const savePermissionsByRoleId = createAsyncThunk(
  "privileges/savePermissions",
  async ({ id, data }, { rejectWithValue }) => {
    try {
      const response = await savePermissions(id, data);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

 // fetch permissions by privilege id
export const getPermissionByPrevId = createAsyncThunk(
  "privileges/getPermissionsById",
  async (id, { rejectWithValue }) => {
    try {
      const response = await getPermissionsById(id);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// Slice definition
const privilegeSlice = createSlice({
  name: "privilege",
  initialState: {
    privileges: {
      privilegeList: [],
      subNames: [],
    },
    privilegeDetails: [],
    listPrivileges: {
      data: [],
      currentPage: 0,
      totalPages: 0,
      totalItems: 0,
      pageSize: 100,
    },
    privilegeData: [],
    selectedPermission: null,
    addLoading: false,
    deleteLoading: false,
    deleteError: null,
    listPrivilegesDropdown: [],
  },
  reducers: {
    clearPrevilegeDetails(state) {
      state.privilegeDetails = null;
      state.error = null;
      state.addError = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // add privileges permissions
      .addCase(addPrivilegesPermissions.pending, (state) => {
        state.addLoading = true;
        state.addError = null;
      })
      .addCase(addPrivilegesPermissions.fulfilled, (state, action) => {
        state.addLoading = false;
        state.privilegeDetails = action.payload;
      })
      .addCase(addPrivilegesPermissions.rejected, (state, action) => {
        state.addLoading = false;
        state.addError = action.payload;
      })

      //list privilege and permissions by role id
      .addCase(getPrivilegesByRoleId.pending, (state) => {
        state.addLoading = true;
        state.error = null;
      })
      .addCase(getPrivilegesByRoleId.fulfilled, (state, action) => {
        state.privilegeDetails = action.payload;
        state.addLoading = false;
      })
      .addCase(getPrivilegesByRoleId.rejected, (state, action) => {
        state.error = action.payload;
        state.addLoading = false;
      })

      //update privileges by id
      .addCase(editPrivilegesById.pending, (state) => {
        state.addLoading = true;
        state.error = null;
      })
      .addCase(editPrivilegesById.fulfilled, (state, action) => {
        state.id = action.payload;
        state.addLoading = false;
      })
      .addCase(editPrivilegesById.rejected, (state, action) => {
        state.error = action.payload;
        state.addLoading = false;
      })

      //list side nav data
      .addCase(getSideNav.pending, (state) => {
        state.addLoading = true;
        state.error = null;
      })
      .addCase(getSideNav.fulfilled, (state, action) => {
        state.subNames = action.payload;
        state.addLoading = false;
      })
      .addCase(getSideNav.rejected, (state, action) => {
        state.error = action.payload;
        state.addLoading = false;
      })

      //get permissions by id
      .addCase(getPermissionById.pending, (state) => {
        state.addLoading = true;
        state.error = null;
      })
      .addCase(getPermissionById.fulfilled, (state, action) => {
        state.selectedPermission = action.payload;
        state.privilegeData = action.payload.data || [];
        state.addLoading = false;
      })

      .addCase(getPermissionById.rejected, (state, action) => {
        state.error = action.payload;
        state.addLoading = false;
      })

      //update privileges by id
      .addCase(editPermissionsById.pending, (state) => {
        state.addLoading = true;
        state.error = null;
      })
      .addCase(editPermissionsById.fulfilled, (state, action) => {
        state.id = action.payload;
        state.addLoading = false;
      })
      .addCase(editPermissionsById.rejected, (state, action) => {
        state.error = action.payload;
        state.addLoading = false;
      })

      //list all privileges
      .addCase(fetchAllPrivileges.pending, (state) => {
        state.addLoading = true;
        state.error = null;
      })
      .addCase(fetchAllPrivileges.fulfilled, (state, action) => {
        state.listPrivileges = action.payload;
        state.addLoading = false;
      })
      .addCase(fetchAllPrivileges.rejected, (state, action) => {
        state.error = action.payload;
        state.addLoading = false;
      })

      // delete privileges
      .addCase(deletePrivilegesById.pending, (state) => {
        state.error = null;
      })
      .addCase(deletePrivilegesById.fulfilled, (state, action) => {
        state.id = action.payload;
      })
      .addCase(deletePrivilegesById.rejected, (state, action) => {
        state.error = action.payload;
      })

      //list all privileges for dropdown
      .addCase(fetchPrivilegesDropdown.pending, (state) => {
        state.addLoading = true;
        state.error = null;
      })
      .addCase(fetchPrivilegesDropdown.fulfilled, (state, action) => {
        state.listPrivilegesDropdown = action.payload;
        state.addLoading = false;
      })
      .addCase(fetchPrivilegesDropdown.rejected, (state, action) => {
        state.error = action.payload;
        state.addLoading = false;
      })

      // Save permission
      .addCase(savePermissionsByRoleId.pending, (state) => {
        state.error = null;
      })
      .addCase(savePermissionsByRoleId.fulfilled, (state, action) => {
        state.id = action.payload;
      })
      .addCase(savePermissionsByRoleId.rejected, (state, action) => {
        state.error = action.payload;
      })

      //get permsisions by privilege id
      .addCase(getPermissionByPrevId.pending, (state) => {
        state.addLoading = true;
        state.error = null;
      })
      .addCase(getPermissionByPrevId.fulfilled, (state, action) => {
        state.privilegeDetails = action.payload;
        state.addLoading = false;
      })
      .addCase(getPermissionByPrevId.rejected, (state, action) => {
        state.error = action.payload;
        state.addLoading = false;
      });
  },
});

export default privilegeSlice.reducer;
