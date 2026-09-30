import { Outlet } from "react-router-dom";
import Header from "./header/Header";
import SideNav from "./sideNav/Sidenav";
import { useState } from "react";

export default function Layout() {
  const [isOpen, setIsOpen] = useState(true); // desktop collapse
  const [mobileOpen, setMobileOpen] = useState(false); // mobile overlay

  const toggleSidebar = () => setIsOpen(!isOpen);
  const toggleMobileSidebar = () => setMobileOpen(!mobileOpen);
  const closeMobileSidebar = () => setMobileOpen(false);

  return (
    <div className="app-layout">
      {/* Fixed Header */}
      <Header onMobileToggle={toggleMobileSidebar} />

      {/* Layout body below header */}
      <div className="layout-body d-flex">
        {/* Sidebar */}
        <SideNav
          isOpen={isOpen}
          toggle={toggleSidebar}
          mobileOpen={mobileOpen}
          closeMobileSidebar={closeMobileSidebar}
        />

        {/* Main Content */}
        <div className="main-content flex-1">
          <div style={{paddingTop:"20px",paddingRight:"2px",paddingLeft:"2px"}} className="content-div">
            <Outlet />
          </div>
        </div>
      </div>
    </div>
  );
}
