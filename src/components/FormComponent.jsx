import PropTypes from "prop-types";
import { Button, Card, Container, Form } from 'react-bootstrap';
import styles from "./form-component.module.css";

/**
 * FormComponent
 *
 * A reusable dynamic form component that renders text fields, select boxes,
 * and other input types based on the configuration passed in the `fields` array.
 *
 * Features:
 * - Supports text, number, email, select, and other input types
 * - Displays validation errors next to each label
 * - Allows fields to be read-only or required
 * - Renders dropdowns with support for object-based and string-based options
 * - Handles submit and input change through parent callbacks
 *
 * Props:
 * - fields: Array defining each form field (label, name, type, etc.)
 * - values: Current form values object
 * - submitText: Text for the submit button
 * - handleSubmit: Function executed when submit button is clicked
 * - handleChange: Function called when field value changes
 * - error: Object containing validation messages for each field
 */
export const FormComponent = ({fields, values, submitText, handleSubmit, handleChange, error }) => {
    return (
        <Container className={`${styles.mainDiv} w-100`}>
            <Card className={`shadow-lg rounded-4 ${styles.divCard}`}>
                <Card.Body className="p-4">
                    {fields.map((field, index) => (
                        <Form.Group className="mb-3" key={field.name} controlId={`field-${index}`}>
                            <Form.Label className={`fw-semibold ${styles.labelStyle}`}>
                                {field.label}
                                {error?.[field.name] && (
                                    <span className='text-danger' style={{ fontSize: "10px" }}>
                                        * {error[field.name]}
                                    </span>
                                )}
                            </Form.Label>

                            {field.type === 'select' ? (
                                <Form.Select
                                    name={field.name}
                                    value={values?.[field.name] ?? ''}
                                    className={styles.borderStyle}
                                    onChange={(e) => handleChange(field.name, e.target.value)}
                                    required
                                    disabled={field.readonly}
                                >
                                    <option value="" disabled>
                                        Select {field.label}
                                    </option>
                                    {field.options?.map((option) =>
                                        typeof option === "object" ? (
                                            <option key={option.id ?? option.currencyCode ?? option.value}>
                                                {option.currencyCode ?? option.label ?? option.value}
                                            </option>
                                        ) : (
                                            <option key={option} value={option}>
                                                {option}
                                            </option>
                                        )
                                    )}
                                </Form.Select>
                            ) : (
                                <Form.Control
                                    name={field.name}
                                    type={field.type}
                                    placeholder={field.placeholder}
                                    value={values?.[field.name] ?? ''}
                                    minLength={field.minLength}
                                    maxLength={field.maxLength}
                                    pattern={field.pattern}
                                    required
                                    readOnly={field.readonly}
                                    className={styles.borderStyle}
                                    onChange={(e) => handleChange(field.name, e.target.value)}
                                />
                            )}
                        </Form.Group>
                    ))}

                    <div className="text-center">
                        <Button
                            className={`px-4 py-2 fw-bold btn btn-success`}
                            onClick={handleSubmit}
                            type="button"
                        >
                            {submitText}
                        </Button>
                    </div>
                </Card.Body>
            </Card>
        </Container>
    )
};

FormComponent.propTypes = {
    fields: PropTypes.arrayOf(
        PropTypes.shape({
            name: PropTypes.string.isRequired,
            label: PropTypes.string.isRequired,
            type: PropTypes.string.isRequired,
            placeholder: PropTypes.string,
            minLength: PropTypes.number,
            maxLength: PropTypes.number,
            pattern: PropTypes.string,
            options: PropTypes.array,
            readonly: PropTypes.bool,
        })
    ).isRequired,
    values: PropTypes.object.isRequired,
    submitText: PropTypes.string.isRequired,
    handleSubmit: PropTypes.func.isRequired,
    handleChange: PropTypes.func.isRequired,
    error: PropTypes.object
};

export default FormComponent;
