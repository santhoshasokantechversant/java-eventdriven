import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import FormComponent from '../../../../../components/FormComponent';
import { ToastMessage } from '../../../../../components/ToastMessage';
import { editAccount, fetchAccountsStatus, fetchAccountTypes, fetchAllCurrency, getAccountById } from '../../../../../redux/slices/accountSlice';
import PropTypes from 'prop-types';

export const EditAccount = ({ account, onClose }) => {
    const dispatch = useDispatch();
    
    // Get current account details from Redux store
    const { currentAccount, currency, accountTypes, accountStatus } = useSelector(state => state.accounts);

    // Toast state for showing success/error messages
    const [toast, setToast] = useState({ show: false, message: "", bg: "success" });

    // Fields definition for FormComponent
    const [fields, setFields] = useState([]);

    // Form values state
    const [values, setValues] = useState({
        accountNumber: "",
        accountBalance: "",
        accountType: "",
        currency: "",
        status: "",
    });

    // Validation errors state
    const [error, setError] = useState({});

    /**
     * Handle field value changes in the form.
     * @param {string} field - The name of the field being updated.
     * @param {any} value - The new value for the field.
     */
    const handleChange = (field, value) => {
        setValues(prev => ({ ...prev, [field]: value }));
    };

    /**
     * Handle form submission for editing an account.
     * Dispatches the editAccount action with updated values.
     */
    const handleSubmit = () => {
        if (validate()) {
            const payload = {
                accountNumber: values.accountNumber,
                balance: values.accountBalance,
                accountType: values.accountType,
                currency: values.currency,
                status: values.status,
            };

            dispatch(editAccount({ id: account.id, account: payload }))
                .unwrap()
                .then((response) => {
                    onClose(true, response.message);
                })
                .catch((error) => {
                    onClose(false, error.message);
                });
        }
    };

    /**
     * Validate form fields.
     * Ensures balance is valid and currency is not empty.
     * @returns {boolean} True if valid, false otherwise.
     */
    const validate = () => {
        const errors = {};
        if (values.accountBalance < 0) {
            errors.accountBalance = "Enter a valid balance";
        }
        if (!values.currency) {
            errors.currency = "Currency cannot be empty";
        }
        setError(errors);
        return Object.keys(errors).length === 0;
    };

    /**
    * Fetch account details, currencies, and account types
    * when the component mounts or account ID changes.
    */
    useEffect(() => {
        dispatch(getAccountById(account.id));
        dispatch(fetchAllCurrency());
        dispatch(fetchAccountTypes());
        dispatch(fetchAccountsStatus());
    }, [account.id, dispatch]);

    /**
     * Populate form fields and values once account,
     * currency list, and account types are loaded.
     */
    useEffect(() => {
        if (!currentAccount || currency.length === 0 || !accountTypes || !accountStatus) return;
        setFields([
            {
                name: "accountNumber",
                label: "Account Number",
                type: "text",
                readonly: true,
            },
            {
                name: "accountBalance",
                label: "Account Balance",
                type: "text",
            },
            {
                name: "accountType",
                label: "Account Type",
                type: "text",
                options: accountTypes,
                readonly: true,
            },
            {
                name: "currency",
                label: "Currency",
                type: "select",
                options: currency,
            },
            {
                name: "status",
                label: "Status",
                type: "select",
                options: accountStatus,
            },
        ]);

        // Pre-fill form values from current account
        setValues({
            accountNumber: currentAccount.accountNumber ?? "",
            accountBalance: currentAccount.balance ?? "",
            accountType: currentAccount.accountType ?? "",
            currency: currentAccount.currency?.currencyCode ?? "",
            status: currentAccount.status ?? ""
        });
    }, [currentAccount, currency, accountTypes, accountStatus]);

    return (
        <>
            <FormComponent
                title="Edit Account"
                fields={fields}
                values={values}
                submitText="Edit"
                handleSubmit={handleSubmit}
                error={error}
                handleChange={handleChange}
            />

            <ToastMessage
                show={toast.show}
                onClose={() => setToast({ ...toast, show: false })}
                message={toast.message}
                bg={toast.bg}
            />
        </>
    );
};

/**
 *  PropTypes validation
 */
EditAccount.propTypes = {
    account: PropTypes.shape({
        id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
        accountNumber: PropTypes.string,
        balance: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
        accountType: PropTypes.string,
        currency: PropTypes.shape({
            currencyCode: PropTypes.string
        }),
        status: PropTypes.string,
    }),

    onClose: PropTypes.func,
};

export default EditAccount;