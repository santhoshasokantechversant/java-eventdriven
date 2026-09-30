import { configureStore } from "@reduxjs/toolkit";
import accountReducer from "./slices/accountSlice";
import authReducer from "./slices/authSlice";
import userReducer from "./slices/userSlice";
import customerReducer from "./slices/customerSlice";
import locationReducer from "./slices/locationSlice";
import roleReducer from "./slices/roleSlice";
import privilegeReducer from "./slices/privilegeSlice";
import endpointsReducer from "./slices/endpointSlice";

// Configure global Redux store and register all feature reducers
const store = configureStore({
  reducer: {
    accounts: accountReducer, // Account module
    auth: authReducer, // Authentication module
    user: userReducer, // User management
    customers: customerReducer, // Customer module
    locations: locationReducer, // Location data
    role: roleReducer, // Role management
    privilege: privilegeReducer, // Privilege/permission management
    endpoints: endpointsReducer, // Endpoint access management
  },
});

export default store;
