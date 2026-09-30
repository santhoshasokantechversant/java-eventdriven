// This file defines the sidebar configuration for the application.
// The backend returns a structured list of modules, each containing:
// - moduleName: Display name in the sidebar
// - slugName: Route to navigate
// - icon: Icon identifier
// - position: Order in the sidebar
// - subPrivileges: Nested menu items (if any)
// - access: Permissions available to the user for this module

const menuConfig = {
    "status": "success",
    "message": "Side-nav fetched successfully",
    "data": [
        {
            "id": "d6b5d9c0-9e47-4eb2-b0eb-77b3d1d4f869",
            "moduleName": "Dashboard",
            "slugName": "/dashboard",
            "icon": "Dashboard",
            "position": 1,
            "subPrivileges": [],
            "access": [
                "DELETE",
                "CREATE",
                "LIST",
                "EDIT"
            ]
        },
        {
            "id": "d6b5d9c0-9e47-4eb2-b0eb-77b3d1d4f869",
            "moduleName": "Users",
            "slugName": "/view-all-users",
            "icon": "Users",
            "position": 2,
            "subPrivileges": [],
            "access": [
                "DELETE",
                "CREATE",
                "LIST",
                "EDIT"
            ]
        },
        {
            "id": "5e98b127-4ad7-4707-8fa4-f7cb0751f71c",
            "moduleName": "Accounts",
            "slugName": "/account-dashboard",
            "icon": "customer",
            "position": 3,
            "subPrivileges": [],
            "access": [
                "DELETE",
                "CREATE",
                "LIST",
                "EDIT"
            ]
        },
        {
            "id": "5e98b127-4ad7-4707-8fa4-f7cb0751f71c",
            "moduleName": "Customer",
            "slugName": "/view-all-customer",
            "icon": "customer",
            "position": 4,
            "subPrivileges": [],
            "access": [
                "DELETE",
                "CREATE",
                "LIST",
                "EDIT"
            ]
        },
        {
            "id": "dfc208d0-61fd-4528-9ed3-2b6910b5a443",
            "moduleName": "Settings",
            "slugName": "/settings-service",
            "icon": "settings",
            "position": 5,
            "subPrivileges": [
                {
                    "id": "8a6b9d5e-bc66-444e-9f16-f845feb1a228",
                    "moduleName": "Roles",
                    "slugName": "/view-all-roles",
                    "icon": "roles",
                    "position": 1,
                    "subPrivileges": [],
                    "access": [
                        "DELETE",
                        "CREATE",
                        "LIST",
                        "EDIT"
                    ]
                },
                {
                    "id": "18937b1c-9217-4f7b-b7ea-e109995c09d8",
                    "moduleName": "Privileges - Permissions",
                    "slugName": "/privileges-permissions-service",
                    "icon": "privileges-permissions",
                    "position": 2,
                    "subPrivileges": [],
                    "access": [
                        "DELETE",
                        "CREATE",
                        "LIST",
                        "EDIT"
                    ]
                }
            ],
            "access": []
        }
    ]
}


export default menuConfig;