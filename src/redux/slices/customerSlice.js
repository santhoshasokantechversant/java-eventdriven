import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import {
  createCustomer as createCustomerAPI,
  deleteCustomerById as deleteCustomerByIdAPI,
  editCustomer as editCustomerAPI,
  fetchAllCustomers as fetchAllCustomersAPI,
  getCustomerById as getCustomerByIdAPI,
} from "../../api/customerApi";


/* -------------------------------------------------------------------------- */
/*                          ASYNC THUNKS – API CALLS                          */
/* -------------------------------------------------------------------------- */

// Fetch all customers thunk
export const fetchAllCustomers = createAsyncThunk(
  "customers/fetchAll",
  async (filter, { rejectWithValue }) => {
    try {
      const response = await fetchAllCustomersAPI(filter);
      if (response.status !== "success") {
        return rejectWithValue(response.message || "Failed to fetch customers");
      }
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// Delete customer thunk
export const deleteCustomer = createAsyncThunk(
  "customers/delete",
  async (id, { rejectWithValue }) => {
    try {
      const response = await deleteCustomerByIdAPI(id);
      const res = response.data;
      res.id = id;
      return res;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message ||
          error.message ||
          "Failed to delete customer"
      );
    }
  }
);

// Fetch single customer by ID thunk
export const fetchCustomerById = createAsyncThunk(
  "customers/fetchById",
  async (id, { rejectWithValue }) => {
    try {
      const response = await getCustomerByIdAPI(id);
      if (response.status !== 200) {
        return rejectWithValue(
          response.data?.message || "Failed to fetch customer"
        );
      }
      if (response.data.status !== "success") {
        return rejectWithValue(
          response.data.message || "Failed to fetch customer"
        );
      }
      return response.data.data;
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message ||
          error.message ||
          "Failed to fetch customer"
      );
    }
  }
);

//Create customer thunk
export const createNewCustomer = createAsyncThunk(
  "customers/create",
  async (customerData, { rejectWithValue }) => {
    try {
      const response = await createCustomerAPI(customerData);
      if (response.status !== 201) {
        return rejectWithValue(
          response.data?.message || "Failed to create customer"
        );
      }
      return {
        customer: response.data,
        message: "Customer saved successfully",
      };
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message ||
          error.message ||
          "Failed to create customer"
      );
    }
  }
);

// Edit/update customer by id
export const editCustomer = createAsyncThunk(
  "customers/edit",
  async (customer, { rejectWithValue }) => {
    try {
      const response = await editCustomerAPI(customer);
      if (response.status !== 200) {
        return rejectWithValue("Failed to update customer");
      }
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || error.message);
    }
  }
);

// Slice definition
const customerSlice = createSlice({
  name: "customers",
  initialState: {
    customers: [],
    selectedCustomer: null,
    currentPage: 0,
    totalPages: 0,
    totalItems: 0,
    pageSize: 10,
    loading: false,
    error: null,
    filter: {
      firstName: "",
      email: "",
      phoneNumber: "",
    },
    message: null,
  },
  reducers: {
    setPage(state, action) {
      state.currentPage = action.payload;
    },
    setFilter(state, action) {
      state.filter = { ...state.filter, ...action.payload };
    },
    resetError(state) {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // fetchAllCustomers
      .addCase(fetchAllCustomers.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAllCustomers.fulfilled, (state, action) => {
        state.loading = false;
        state.customers = action.payload.customers;
        state.currentPage = action.payload.currentPage;
        state.totalPages = action.payload.totalPages;
        state.totalItems = action.payload.totalItems;
        state.pageSize = action.payload.pageSize;
      })
      .addCase(fetchAllCustomers.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Something went wrong";
      })

      // deleteCustomer by id soft deletion
      .addCase(deleteCustomer.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteCustomer.fulfilled, (state, action) => {
        state.loading = false;
        state.customers = state.customers.filter(
          (customer) => customer.id !== action.payload.id
        );
        state.totalItems -= 1;
        state.message=action.payload.message;
      })
      .addCase(deleteCustomer.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to delete customer";
      })

      // fetchCustomerById
      .addCase(fetchCustomerById.pending, (state) => {
        state.loading = true;
        state.error = null;
        state.selectedCustomer = null;
      })
      .addCase(fetchCustomerById.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedCustomer = action.payload;
      })
      .addCase(fetchCustomerById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to fetch customer";
        state.selectedCustomer = null;
      })

      // createNewCustomer
      .addCase(createNewCustomer.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createNewCustomer.fulfilled, (state, action) => {
        state.loading = false;
        state.customers.unshift(action.payload.customer);
        state.totalItems += 1;
      })
      .addCase(createNewCustomer.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to create customer";
      })

      // editCustomer
      .addCase(editCustomer.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(editCustomer.fulfilled, (state, action) => {
        state.loading = false;
        const index = state.customers.findIndex(
          (c) => c.customerNo === action.payload.customerNo
        );
        if (index !== -1) state.customers[index] = action.payload;

        if (state.selectedCustomer?.customerNo === action.payload.customerNo) {
          state.selectedCustomer = action.payload;
        }
      })
      .addCase(editCustomer.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to update customer";
      });
  },
});

export const { setPage, setFilter, resetError } = customerSlice.actions;

export default customerSlice.reducer;
