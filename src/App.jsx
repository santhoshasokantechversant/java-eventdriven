import { BrowserRouter, Route, Routes, Navigate } from "react-router-dom";
import "./App.css";
import authRoutes from "./features/auth/routes/AuthRoutes.jsx";
import mainRoutes from "./features/main/routes/MainRoute.jsx";
import ProtectedRoute from "./components/common/ProtectedRoute.jsx";
import Layout from "./components/layout/Layout.jsx";
import { ToastContainer } from "react-toastify";
import RefreshTokenConfirmModal from "./components/RefreshTokenConfirmModal.jsx";

const App = () => {
  return (
    <BrowserRouter>
      <Routes>
        {/* Auth routes (public) */}
        {authRoutes.map(({ path, element }) => (
          <Route key={path} path={path} element={element} />
        ))}

        {/* Protected routes wrapped in Layout */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          {mainRoutes.map(({ path, element }) => (
            <Route key={path} path={path} element={element} />
          ))}

          {/* Optional: default redirect */}
          <Route index element={<Navigate to="/dashboard" />} />

          {/* Optional: fallback */}
          <Route path="*" element={<h1>Page Not Found</h1>} />
        </Route>
      </Routes>
      <ToastContainer position="top-right" autoClose={3000} />
      <RefreshTokenConfirmModal />
    </BrowserRouter>
  );
};

export default App;
