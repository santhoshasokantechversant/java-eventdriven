import { useState, useEffect, useRef } from "react";
import { NavLink } from "react-router-dom";
import icons from "../../../config/icons";
import "./Sidenav.css";
import PropTypes from 'prop-types';

function SidebarItem({ item, isOpen, closeMobileSidebar }) {
  const [expanded, setExpanded] = useState(false);
  const hasChildren = item.subPrivileges && item.subPrivileges.length > 0;
  const foundIcon = icons.find((i) => i.value === item.icon);
  const IconElement = foundIcon ? foundIcon.icon : null;

  const toggleSubmenu = () => setExpanded(!expanded);

  if (hasChildren) {
    return (
      <div className="sidebar-item">
        <div>
          <button 
          type="button"
          className="sidebar-link d-flex align-items-center" onClick={toggleSubmenu} style={{ cursor: "pointer" }}>
            <span className="sidebar-icon">{IconElement}</span>
            {isOpen && <span className="ms-2">{item.moduleName}</span>}
            {isOpen && <span className="ms-auto">{expanded ? "▲" : "▼"}</span>}
          </button>
        </div>

        {expanded && (
          <div className="submenu ps-3">
            {item.subPrivileges
              .sort((a, b) => a.position - b.position)
              .map((subItem) => (
                <SidebarItem
                  key={subItem.id}
                  item={subItem}
                  isOpen={isOpen}
                  closeMobileSidebar={closeMobileSidebar}
                />
              ))}
          </div>
        )}
      </div>
    );
  }

  return (
    <NavLink
      to={item.slugName}
      className={({ isActive }) =>
        `sidebar-link ${isActive ? "active-link" : ""} d-flex align-items-center`
      }
      onClick={closeMobileSidebar}
    >
      <span className="sidebar-icon">{IconElement}</span>
      {isOpen && <span className="ms-2">{item.moduleName}</span>}
    </NavLink>
  );
}

export default function SideNav({
  isOpen,
  toggle,
  mobileOpen,
  closeMobileSidebar,
}) {
  const [menuItems, setMenuItems] = useState([]);
  const overlayRef = useRef();

  useEffect(() => {
    const storedNav = sessionStorage.getItem("sideNav");
    if (storedNav) {
      try {
        const parsedNav = JSON.parse(storedNav);
        setMenuItems(parsedNav.sort((a, b) => a.position - b.position));
      } catch (err) {
        console.error("Failed to parse sideNav:", err);
      }
    }
  }, []);

  useEffect(() => {
    function handleClickOutside(event) {
      if (
        overlayRef.current &&
        !overlayRef.current.contains(event.target) &&
        mobileOpen
      ) {
        closeMobileSidebar();
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () =>
      document.removeEventListener("mousedown", handleClickOutside);
  }, [mobileOpen, closeMobileSidebar]);

  const [collapsedByResize, setCollapsedByResize] = useState(false);

  useEffect(() => {
    function handleResize() {
      if (window.innerWidth < 1100 && isOpen && !collapsedByResize) {
        toggle(); // collapse once due to small screen
        setCollapsedByResize(true);
      } else if (window.innerWidth >= 1100 && collapsedByResize) {
        // Reset the flag so user can open manually
        setCollapsedByResize(false);
      }
    }

    window.addEventListener("resize", handleResize);
    handleResize(); // check initial width
    return () => window.removeEventListener("resize", handleResize);
  }, [isOpen, toggle, collapsedByResize]);



  return (
    <>
      {/* Desktop sidebar */}
      <div
        className={`sidebar-custom ${isOpen ? "sidebar-open" : "sidebar-collapsed"
          } d-none d-md-flex`}
      >
        <div className="toggle-container">
          <button
          type="button"
            className="sidebar-toggle-btn"
            onClick={toggle}
            title={isOpen ? "Collapse Sidebar" : "Expand Sidebar"}
          >
            <span className={`arrow ${isOpen ? "left" : "right"}`}></span>
          </button>
        </div>

        <div className="menu-scroll">
          {menuItems.map((item) => (
            <SidebarItem
              key={item.id}
              item={item}
              isOpen={isOpen}
              closeMobileSidebar={closeMobileSidebar}
            />
          ))}
        </div>
      </div>

      {/* Mobile overlay */}
      {mobileOpen && (
        <div className="mobile-overlay">
          <div className="mobile-menu" ref={overlayRef}>
            {menuItems.map((item) => (
              <SidebarItem
                key={item.id}
                item={item}
                isOpen={true}
                closeMobileSidebar={closeMobileSidebar}
              />
            ))}
          </div>
        </div>
      )}
    </>
  );
}

SideNav.propTypes = {
  item: PropTypes.shape({
    label: PropTypes.string.isRequired,
    icon: PropTypes.element, // optional
    path: PropTypes.string,  // optional
  }).isRequired,

  isOpen: PropTypes.bool.isRequired,
  mobileOpen: PropTypes.bool,  
  toggle: PropTypes.func.isRequired,
  closeMobileSidebar: PropTypes.func.isRequired,
}