import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import {
  deleteAccount,
  editAccountById,
  fetchAccountByCustomerId,
  fetchAccountById,
  fetchAccountStatus,
  fetchAccountType,
  fetchAllAccounts,
  fetchCurrency,
} from "../../api/accountApi.js";

/* -------------------------------------------------------------------------- */
/*                          ASYNC THUNKS – API CALLS                          */
/* -------------------------------------------------------------------------- */

// Fetch all accounts with filters (pagination, search, etc.)
export const fetchAccounts = createAsyncThunk(
  "accounts/fetchAllAcounts",
  async ( filter , { rejectWithValue }) => {
    try {
      const response = await fetchAllAccounts(filter);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || "Something went wrong");
    }
  }
);

// Fetch all account types
export const fetchAccountTypes = createAsyncThunk(
  "accounts/fetchAccountTypes",
  async (_, { rejectWithValue }) => {
    try {
      const response = await fetchAccountType();
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || "Something went wrong");
    }
  }
);

// Fetch all supported currency list
export const fetchAllCurrency = createAsyncThunk(
  "accounts/fetchAllCurrency",
  async (_, { rejectWithValue }) => {
    try {
      const response = await fetchCurrency();
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || "Something went wrong");
    }
  }
);

// Delete an account by ID
export const deleteAccountById = createAsyncThunk(
  "accounts/deleteAccountById",
  async (id, { rejectWithValue }) => {
    try {
      const response = await deleteAccount(id);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || "Something went wrong");
    }
  }
);

// Edit / update an account by ID
export const editAccount = createAsyncThunk(
  "accounts/editAccount",
  async ({ id, account }, { rejectWithValue }) => {
    try {
      const response = await editAccountById(id, account);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || "Something went wrong");
    }
  }
);

// Get single account details by ID
export const getAccountById = createAsyncThunk(
  "accounts/getAccountById",
  async (id, { rejectWithValue }) => {
    try {
      const response = await fetchAccountById(id);
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || "Something went wrong");
    }
  }
);

// Get accounts using customer ID
export const getAccountByCustomerId = createAsyncThunk(
  "accounts/fetchAccountByCustomerId",
  async (id, { rejectWithValue }) => {
    try {
      const response = await fetchAccountByCustomerId(id); 
      return response; 
    } catch (error) {
      return rejectWithValue(error.response?.data || "Something went wrong");
    }
  }
);

// Fetch account status list
export const fetchAccountsStatus = createAsyncThunk(
  "accounts/fetchAccountStatus",
  async (_, { rejectWithValue }) => {
    try {
      const response = await fetchAccountStatus();
      return response;
    } catch (error) {
      return rejectWithValue(error.response?.data || "Something went wrong");
    }
  }
);

// slice definition

const accountSlice = createSlice({
  name: "accounts",
  initialState: {
    accounts: {
      account: [],
      currentPage: 0,
      pageSize: 10,
      totalItems: 0,
      totalPages: 0,
    },
    accountTypes: [],
    currency: [],
    currentAccount: null,
    loading: false,
    error: null,
    accountStatus: [],
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchAccounts.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAccounts.fulfilled, (state, action) => {
        state.loading = false;
        state.accounts = action.payload;
      })
      .addCase(fetchAccounts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Fetch Account Types
      .addCase(fetchAccountTypes.pending, (state) => {
        state.error = null;
      })
      .addCase(fetchAccountTypes.fulfilled, (state, action) => {
        state.accountTypes = action.payload; // store account types
      })
      .addCase(fetchAccountTypes.rejected, (state, action) => {
        state.error = action.payload;
      })

      // Fetch Currency
      .addCase(fetchAllCurrency.pending, (state) => {
        state.error = null;
      })
      .addCase(fetchAllCurrency.fulfilled, (state, action) => {
        state.currency = action.payload; // store account types
      })
      .addCase(fetchAllCurrency.rejected, (state, action) => {
        state.error = action.payload;
      })

      // delete account by id
      .addCase(deleteAccountById.pending, (state) => {
        state.error = null;
      })
      .addCase(deleteAccountById.fulfilled, (state, action) => {
        state.id = action.payload; // store account types
      })
      .addCase(deleteAccountById.rejected, (state, action) => {
        state.error = action.payload;
      })

      //update account by id
      .addCase(editAccount.pending, (state) => {
        state.error = null;
      })
      .addCase(editAccount.fulfilled, (state, action) => {
        state.id = action.payload;
      })
      .addCase(editAccount.rejected, (state, action) => {
        state.error = action.payload;
      })

      //get account by id
      .addCase(getAccountById.pending, (state) => {
        state.error = null;
      })
      .addCase(getAccountById.fulfilled, (state, action) => {
        state.currentAccount = action.payload;
      })
      .addCase(getAccountById.rejected, (state, action) => {
        state.error = action.payload;
      })

         //get account details by customer id
      .addCase(getAccountByCustomerId.pending, (state) => {
        state.error = null;
      })
      .addCase(getAccountByCustomerId.fulfilled, (state, action) => {
        state.currentAccount  = action.payload;
      })
      .addCase(getAccountByCustomerId.rejected, (state, action) => {
        state.error = action.payload;
      })

      // Fetch Account Status
      .addCase(fetchAccountsStatus.pending, (state) => {
        state.error = null;
      })
      .addCase(fetchAccountsStatus.fulfilled, (state, action) => {
        state.accountStatus = action.payload; // store account types
      })
      .addCase(fetchAccountsStatus.rejected, (state, action) => {
        state.error = action.payload;
      });
  },
});

export default accountSlice.reducer;
