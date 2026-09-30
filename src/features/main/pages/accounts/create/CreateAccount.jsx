import React, { useState } from 'react'
import FormComponent from '../../../../../components/FormComponent';

export function CreateAccount() {
     // Form field configuration
    const addAccountFields = [
        { name: "accountNumber", label: "Account Number", type: "text", placeholder: "Enter account number" },
        { name: "accountBalance", label: "Account Balance", type: "text", placeholder: "Enter balance" },
        { name: "accountType", label: "Account Type", type: "select", options: ["Savings", "Current", "Fixed Deposit"], defaultValue: "" },
        { name: "currency", label: "Currency", type: "text", placeholder: "Enter currency" },
    ];

    // Form state
    const [result, setResult] = useState({
        accountNumber: "",
        accountBalance: "",
        accountType: "",
        currency: ""
    });

    // Validation error state
    const [error, setError] = useState({
        accountNumber: "",
        accountBalance: "",
        accountType: "",
        currency: ""
    });

    // Update field values
    const handleChange = (field, value) => {
        setResult(prev => ({ ...prev, [field]: value }));
    }

    // Form submit handler
    const handleSubmit = () => {
        if (validate()) {
            console.log("Form submitted successfully", result);
        }
    };

    // Form validation
    const validate = () => {
        const errors = {};

        if (!/^\d{11}$/.test(result.accountNumber)) {
            errors.accountNumber = "Account Number must be exactly 11 digits and numeric";
        }
        if (!/^\d+(\.\d{1,2})?$/.test(result.accountBalance)) {
            errors.accountBalance = "Enter a valid balance (up to 2 decimal places)";
        }

        if (result.accountType == "") {
            errors.accountType = "Account type not empty";
        }
        if (result.currency == "") {
            errors.currency = "Currency not empty";
        }
        setError(errors);
        return Object.keys(errors).length === 0;
    }

    return (
        <div>
            <FormComponent title="Account Creation" fields={addAccountFields} submitText="Create" handleSubmit={handleSubmit} handleChange={handleChange} error={error} />
        </div>

    )
}
export default CreateAccount;