import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import {
  getCountriesForRegion,
  getStatesForCountry,
  getCityForState,
} from "../../api/locationApi";


/* -------------------------------------------------------------------------- */
/*                          ASYNC THUNKS – API CALLS                          */
/* -------------------------------------------------------------------------- */

// Thunk to fetch countries
export const fetchCountriesForRegion = createAsyncThunk(
  "locations/fetchCountriesForRegion",
  async (_, { rejectWithValue }) => {
    try {
      const response = await getCountriesForRegion();
      if (response.status !== 200) {
        return rejectWithValue("Failed to fetch countries");
      }
      return response.data.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || error.message);
    }
  }
);

// Thunk to fetch states by country ID
export const fetchStatesForCountry = createAsyncThunk(
  "locations/fetchStatesForCountry",
  async (countryId, { rejectWithValue }) => {
    try {
      const response = await getStatesForCountry(countryId);
      if (response.status !== 200) {
        return rejectWithValue("Failed to fetch states");
      }
      return response.data.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || error.message);
    }
  }
);

// Thunk to fetch cities by state ID
export const fetchCitiesForState = createAsyncThunk(
  "locations/fetchCitiesForState",
  async (stateId, { rejectWithValue }) => {
    try {
      const response = await getCityForState(stateId);
      if (response.status !== 200) {
        return rejectWithValue("Failed to fetch cities");
      }
      return response.data.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || error.message);
    }
  }
);

// Slice definition
const locationSlice = createSlice({
  name: "locations",
  initialState: {
    countries: [],
    states: [],
    cities: [],
    loading: false,
    error: null,
  },
  reducers: {
    resetLocationError(state) {
      state.error = null;
    },
    clearStates(state) {
      state.states = [];
    },
    clearCities(state) {
      state.cities = [];
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch countries
      .addCase(fetchCountriesForRegion.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCountriesForRegion.fulfilled, (state, action) => {
        state.loading = false;
        state.countries = action.payload;
      })
      .addCase(fetchCountriesForRegion.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to fetch countries";
      })

      // Fetch states
      .addCase(fetchStatesForCountry.pending, (state) => {
        state.loading = true;
        state.error = null;
        state.states = [];
      })
      .addCase(fetchStatesForCountry.fulfilled, (state, action) => {
        state.loading = false;
        state.states = action.payload;
      })
      .addCase(fetchStatesForCountry.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to fetch states";
      })

      // Fetch cities
      .addCase(fetchCitiesForState.pending, (state) => {
        state.loading = true;
        state.error = null;
        state.cities = [];
      })
      .addCase(fetchCitiesForState.fulfilled, (state, action) => {
        state.loading = false;
        state.cities = action.payload;
      })
      .addCase(fetchCitiesForState.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to fetch cities";
      });
  },
});

export const { resetLocationError, clearStates, clearCities } =
  locationSlice.actions;

export default locationSlice.reducer;
