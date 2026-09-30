import {
    Bank, 
    Gear, 
    Key,
    People, 
    PersonCircle, 
    ShieldLock
} from "react-bootstrap-icons";
import { FaHome } from "react-icons/fa";

/**
 * List of sidebar/menu icons used across the application.
 * 
 * Each object contains:
 * - id: Unique numeric identifier
 * - value: The label to display in the UI
 * - icon: The actual icon component to render
 * 
 * This array keeps the sidebar dynamic, centralized, and easily maintainable.
 */
const icons = [
    {
        id: 1,
        value: "Dashboard",
        icon: <FaHome />
    },
    {
        id: 2,
        value: "Users",
        icon: <People />
    },
    {
        id: 3,
        value: "Accounts",
        icon: <Bank />
    },
    {
        id: 4,
        value: "Customer",
        icon: <PersonCircle />
    },
    {
        id: 5,
        value: "Settings",
        icon: <Gear />
    },
    {
        id: 6,
        value: "Roles",
        icon: <Key />
    },
    {
        id: 7,
        value: "Privileges - Permissions",
        icon: <ShieldLock />
    }
];

export default icons;
