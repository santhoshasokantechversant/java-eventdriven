import { useEffect, useState, useRef } from "react";
import { Container, Dropdown } from "react-bootstrap";
import { PersonCircle, Power } from "react-bootstrap-icons";
import { useDispatch } from "react-redux";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { logoutUser } from "../../../redux/slices/authSlice";
import "./Header.css";
import { BsArrowRightCircleFill, BsList } from "react-icons/bs";
import PropTypes from 'prop-types';

export default function Header({ onMobileToggle }) {

  const [fullName, setFullName] = useState("");
  const [showMobileMenu, setShowMobileMenu] = useState(false);
  const profileMenuRef = useRef(); // Added ref for mobile profile menu

  const dispatch = useDispatch();
  const navigate = useNavigate();

  // Load user name from sessionStorage when component mounts
  useEffect(() => {
    const name = sessionStorage.getItem("fullName");
    setFullName(name ? String(name) : " ");
  }, []);

  // Logout handler
  const handleLogout = async () => {
  try {
    await dispatch(logoutUser()).unwrap();

    // Logout successful → redirect to login
    navigate("/user-login", { replace: true });
  } catch (err) {
    toast.error(`Logout failed: ${JSON.stringify(err)}`);
  }
};

  // Close mobile profile menu when clicking outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (
        profileMenuRef.current &&
        !profileMenuRef.current.contains(event.target)
      ) {
        setShowMobileMenu(false);
      }
    }

    if (showMobileMenu) {
      document.addEventListener("mousedown", handleClickOutside);
    } else {
      document.removeEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [showMobileMenu]);

  return (
    <header className="app-header d-flex align-items-center p-3">
      <Container
        fluid
        className="d-flex align-items-center justify-content-between"
      >
        {/* Left Side */}
        <div className="d-flex align-items-center">
          <button
          type="button"
            className="hamburger-btn d-md-none me-2"
            onClick={onMobileToggle}
          >
            <BsArrowRightCircleFill size={28} />
          </button>
          <h4 className="mb-0">Banking App</h4>
        </div>

        {/* Right Side - Desktop */}
        <div className="d-none d-md-flex align-items-center">
          <div className="d-flex align-items-center me-4">
            <h6 className="mb-0 me-2">Hello!</h6>
            <h6 className="mb-0 fw-semibold">{fullName}</h6>
          </div>

          <Dropdown align="end">
            <Dropdown.Toggle
              as="div"
              id="dropdown-profile"
              className="border-0 bg-transparent p-0 dropdown-toggle"
              style={{ cursor: "pointer" }}
            >
              <PersonCircle size={28} className="me-3 text-dark" />
            </Dropdown.Toggle>

            <Dropdown.Menu>
              <Dropdown.Item as={Link} to="/view-profile">
                View Profile
              </Dropdown.Item>
              <hr />
              <Dropdown.Item as={Link} to="/change-password">
                Change Password
              </Dropdown.Item>
            </Dropdown.Menu>
          </Dropdown>

          <Power
            size={24}
            style={{ cursor: "pointer" }}
            onClick={handleLogout}
          />
        </div>

        {/* Right Side - Mobile */}
        <div className="d-md-none position-relative">
          <BsList
            size={28}
            style={{ cursor: "pointer" }}
            onClick={() => setShowMobileMenu(!showMobileMenu)}
          />
          {showMobileMenu && (
            <div
              className="mobile-dropdown mt-2 p-3 shadow-sm rounded bg-light"
              ref={profileMenuRef}
            >
              <div className="d-flex flex-column text-center">
                <h6 className="fw-semibold mb-2">{fullName}</h6>
                <Link
                  to="/view-profile"
                  className="text-decoration-none mb-2"
                  onClick={() => setShowMobileMenu(false)}
                >
                  View Profile
                </Link>
                <Link
                  to="/change-password"
                  className="text-decoration-none mb-2"
                  onClick={() => setShowMobileMenu(false)}
                >
                  Change Password
                </Link>
                <button
                type="button"
                  className="btn btn-outline-danger btn-sm mt-2"
                  onClick={() => {
                    setShowMobileMenu(false);
                    handleLogout();
                  }}
                >
                  Logout
                </button>
              </div>
            </div>
          )}
        </div>
      </Container>
    </header>
  );
};

Header.propTypes = {
  onMobileToggle: PropTypes.func,
};