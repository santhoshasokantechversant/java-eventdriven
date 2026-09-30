import ForbiddenError from "../../../components/pages/ForbiddenError.jsx";
import { UnderDevelopment } from "../../../components/pages/UnderDevelopment.jsx";
import CreateAccount from "../pages/accounts/create/CreateAccount";
import AccountDashboard from "../pages/accounts/list/AccountDashboard.jsx";
import { ViewAccount } from "../pages/accounts/list/ViewAccount.jsx";
import EditAccount from "../pages/accounts/update/EditAccount.jsx";
import { CreateCustomer } from "../pages/customers/create/CreateCustomer.jsx";
import ViewAllCustomer from "../pages/customers/list/ViewAllCustomer.jsx";
import { ViewCustomer } from "../pages/customers/list/ViewCustomer.jsx";
import { EditCustomer } from "../pages/customers/update/EditCustomer.jsx";
import AdminManagerDashboard from "../pages/dashboard/AdminManagerDashboard.jsx";
import { CustomerDashboard } from "../pages/dashboard/CustomerDashboard.jsx";
import { CreatePrivilegesPermissions } from "../pages/privileges/create/CreatePrivilegesPermissions.jsx";
import { PrivilegesPermissions } from "../pages/privileges/list/PrivilegesPermissions.jsx";
import { UpdatePrivilegesPermissions } from "../pages/privileges/update/UpdatePrivilegesPermissions.jsx";
import { ViewAllEndpoints } from "../pages/privilegesNew/endpoints/list/ViewAllEndpoints.jsx";
import { ViewAllPermissions } from "../pages/privilegesNew/permissions/list/ViewAllPermissions.jsx";
import { ViewAllPrivileges } from "../pages/privilegesNew/privileges/list/ViewAllPrivileges.jsx";
import { PrivilegesDashboard } from "../pages/privilegesNew/PrivilegesDashboard.jsx";
import ViewProfile from "../pages/profile/list/ViewProfile.jsx";
import { ChangePassword } from "../pages/profile/update/ChangePassword.jsx";
import CreateRole from "../pages/role/create/CreateRole.jsx";
import ViewAllRoles from "../pages/role/list/ViewAllRoles.jsx";
import EditRole from "../pages/role/update/EditRole.jsx";
import CreateUser from "../pages/users/create/CreateUser.jsx";
import ViewAllUsers from "../pages/users/list/ViewAllUsers.jsx";
import ViewUser from "../pages/users/list/ViewUser.jsx";
import EditUser from "../pages/users/update/EditUser.jsx";

const mainRoutes = [
  { path: "/dashboard", element: <AdminManagerDashboard /> },
  { path: "/view-all-users", element: <ViewAllUsers /> },
  { path: "/create-account", element: <CreateAccount /> },
  { path: "/account-dashboard", element: <AccountDashboard /> },
  { path: "/edit-account/:id", element: <EditAccount /> },
  { path: "/user-view/:id", element: <ViewUser /> },
  { path: "/edit-user/:id", element: <EditUser /> },
  { path: "/create-role", element: <CreateRole /> },
  { path: "/view-all-users", element: <ViewAllUsers /> },
  // { path: "/user-dashboard", element: <UserDashboard /> },
  { path: "/view-all-roles", element: <ViewAllRoles /> },
  { path: "/edit-role/:id", element: <EditRole /> },
  { path: "/forbidden-error", element: <ForbiddenError /> },
  { path: "/create-customer", element: <CreateCustomer /> },
  { path: "/edit-customer", element: <EditCustomer /> },
  { path: "/view-all-customer", element: <ViewAllCustomer /> },
  { path: "/admin-manager-dashboard", element: <AdminManagerDashboard /> },
  { path: "/customer-dashboard", element: <CustomerDashboard /> },
  { path: "/under-development", element: <UnderDevelopment /> },
  { path: "/Testing-1-service", element: <h1>Hello Test 123</h1> },
  { path: "/view-customer/:id", element: <ViewCustomer /> },
  { path: "/view-account/:id", element: <ViewAccount /> },
  { path: "/privileges-permissions-service", element: <PrivilegesPermissions /> },
  { path: "/create-user", element: <CreateUser /> },
  { path: "/create-privileges-permissions", element: <CreatePrivilegesPermissions /> },
  { path: "/view-profile", element: <ViewProfile /> },
  { path: "/change-password", element: <ChangePassword /> },
  { path: "/update-privileges-permissions/:id", element: <UpdatePrivilegesPermissions /> },
  { path: "/privileges-dashboard", element: <PrivilegesDashboard /> },
  { path: "/view-all-privileges", element: <ViewAllPrivileges /> },
  { path: "/view-all-permissions", element: <ViewAllPermissions /> },
  { path: "/view-all-endpoints", element: <ViewAllEndpoints /> },
]

export default mainRoutes;