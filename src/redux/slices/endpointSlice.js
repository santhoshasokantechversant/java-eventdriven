import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import {
  addEndpoints,
  deleteEndpoints,
  editEndpoints,
  listAllEndpoints,
  listEndpointsById,
} from "../../api/endpointApi";

/* -------------------------------------------------------------------------- */
/*                          ASYNC THUNKS – API CALLS                          */
/* -------------------------------------------------------------------------- */

// create new endpoints
export const createEndpoints = createAsyncThunk(
  "endpoints/addEndpoints",
  async (data, { rejectWithValue }) => {
    try {
      const response = await addEndpoints(data);

      return response;
    } catch (error) {
       return rejectWithValue(error.response?.data || "Something went wrong");
    }
  }
);

// fetch all endpoints
export const fetchAllEndpoints = createAsyncThunk(
  "endpoints/fetchAllEndpoints",
  async (data, { rejectWithValue }) => {
    try {
      const response = await listAllEndpoints(data);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || "Something went wrong");
    }
  }
);

// edit/update endpoints by id
export const editEndpointsById = createAsyncThunk(
  "endpoints/editEndpointsById",
  async ({ id, data }, { rejectWithValue }) => {
    try {
      const response = await editEndpoints(id, data);
      return response;
    } catch (error) {
       return rejectWithValue(error.response?.data || "Something went wrong");
    }
  }
);

// fetch endpoints by id
export const getEndpointsById = createAsyncThunk(
  "endpoints/getEndpointsById",
  async (id, { rejectWithValue }) => {
    try {
      const response = await listEndpointsById(id);
      return response;
    } catch (error) {
       return rejectWithValue(error.response?.data || "Something went wrong");
    }
  }
);

// delete endpoints by id soft deletion
export const deleteEndpointsById = createAsyncThunk(
  "endpoints/deleteEndpointsById",
  async (id, { rejectWithValue }) => {
    try {
      const response = await deleteEndpoints(id);
      return { id, response };
    } catch (error) {
       return rejectWithValue(error.response?.data || "Something went wrong");
    }
  }
);

// Slice definition
const endpointSlice = createSlice({
  name: "endpoints",
  initialState: {
    endpointDetails: [],
    addLoading: false,
    addError: null,
    deleteLoading: false,
    listEndpoints: {
      data: [],
      currentPage: 0,
      totalPages: 0,
      totalItems: 0,
      pageSize: 10,
    },
    endpointData: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      // add endpoints
      .addCase(createEndpoints.pending, (state) => {
        state.addLoading = true;
        state.addError = null;
      })
      .addCase(createEndpoints.fulfilled, (state, action) => {
        state.addLoading = false;
        state.endpointDetails = action.payload;
      })
      .addCase(createEndpoints.rejected, (state, action) => {
        state.addLoading = false;
        state.addError = action.payload;
      })

      //list all endpoints
      .addCase(fetchAllEndpoints.pending, (state) => {
        state.addLoading = true;
        state.error = null;
      })
      .addCase(fetchAllEndpoints.fulfilled, (state, action) => {
        state.listEndpoints = action.payload;
        state.addLoading = false;
      })
      .addCase(fetchAllEndpoints.rejected, (state, action) => {
        state.error = action.payload;
        state.addLoading = false;
      })
      //update endpoints by id
      .addCase(editEndpointsById.pending, (state) => {
        state.addLoading = true;
        state.error = null;
      })
      .addCase(editEndpointsById.fulfilled, (state, action) => {
        state.id = action.payload;
        state.addLoading = false;
      })
      .addCase(editEndpointsById.rejected, (state, action) => {
        state.error = action.payload;
        state.addLoading = false;
      })

      //get endpoints by id
      .addCase(getEndpointsById.pending, (state) => {
        state.addLoading = true;
        state.error = null;
      })
      .addCase(getEndpointsById.fulfilled, (state, action) => {
        state.endpointData = action.payload;
        state.addLoading = false;
      })

      .addCase(getEndpointsById.rejected, (state, action) => {
        state.error = action.payload;
        state.addLoading = false;
      })

      // delete endpoints
      .addCase(deleteEndpointsById.pending, (state) => {
        state.error = null;
      })
      .addCase(deleteEndpointsById.fulfilled, (state, action) => {
        state.id = action.payload;
      })
      .addCase(deleteEndpointsById.rejected, (state, action) => {
        state.error = action.payload;
      });
  },
});

export default endpointSlice.reducer;
