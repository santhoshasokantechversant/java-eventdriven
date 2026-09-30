import React from "react";
import PropTypes from "prop-types";

/**
 * PaginationControls Component
 * ----------------------------
 * Reusable pagination UI for navigating between pages.
 *
 * Props:
 * - currentPage (number): The currently active page (0-based index).
 * - totalPages (number): Total number of pages available.
 * - onPageChange (function): Callback function triggered when a page is changed.
 *
 * This component renders:
 * - Previous button
 * - Numbered page buttons
 * - Next button
 */
const PaginationControls = ({ currentPage, totalPages, onPageChange }) => {
  const pages = [];
  for (let i = 0; i < totalPages; i++) {
    pages.push(i);
  }

  return (
    <nav>
      <ul className="pagination justify-content-center">
        {/* Previous Button */}
        <li className={`page-item ${currentPage === 0 ? "disabled" : ""}`}>
          <button
          type="button"
            className="page-link"
            onClick={() => onPageChange(currentPage - 1)}
            disabled={currentPage === 0}
          >
            Previous
          </button>
        </li>

        {/* Page Numbers */}
        {pages.map((pageNum) => (
          <li
            key={pageNum}
            className={`page-item ${pageNum === currentPage ? "active" : ""}`}
          >
            <button 
            type="button"
            className="page-link" onClick={() => onPageChange(pageNum)}>
              {pageNum + 1}
            </button>
          </li>
        ))}

        {/* Next Button */}
        <li
          className={`page-item ${currentPage === totalPages - 1 ? "disabled" : ""
            }`}
        >
          <button
          type="button"
            className="page-link"
            onClick={() => onPageChange(currentPage + 1)}
            disabled={currentPage === totalPages - 1}
          >
            Next
          </button>
        </li>
      </ul>
    </nav>
  );
};

// Add prop type validation 
PaginationControls.propTypes = {
  currentPage: PropTypes.number.isRequired,
  totalPages: PropTypes.number.isRequired,
  onPageChange: PropTypes.func.isRequired,
};

export default PaginationControls;
