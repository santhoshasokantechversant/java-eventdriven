import React from "react";
import { Form, Pagination } from "react-bootstrap";
import styles from "./custom-pagination.module.css";
import PropTypes from "prop-types";

/**
 * AdvancedPagination Component
 *
 * This component provides a customizable and user-friendly pagination UI.
 * It supports:
 * - Dynamic page number generation with ellipsis (…)
 * - First, Previous, Next, Last navigation buttons
 * - Page size selection (10, 25, 50, 100)
 * - Displaying item range like "Showing 11–20 of 100 items"
 *
 * Props:
 * - currentPage: Current active page (0-based index)
 * - totalPages: Total number of pages available
 * - onPageChange: Function called when user selects a new page
 * - totalItems: Total number of records/items
 * - pageSize: Number of items per page
 * - handlePageSizeChange: Callback to update page size
 */
const AdvancedPagination = ({
  currentPage,
  totalPages,
  onPageChange,
  totalItems,
  pageSize,
  handlePageSizeChange,
}) => {
  if (!totalPages) return null;

  const generatePages = () => {
    const pages = [];
    const delta = 2;
    const range = [];

    for (let i = 1; i <= totalPages; i++) {
      if (
        i === 1 ||
        i === totalPages ||
        (i >= currentPage + 1 - delta && i <= currentPage + 1 + delta)
      ) {
        range.push(i);
      }
    }

    let l;
    range.forEach((i) => {
      if (l) {
        if (i - l === 2) {
          pages.push(
            <Pagination.Item key={l + 1} onClick={() => onPageChange(l)}>
              {l + 1}
            </Pagination.Item>
          );
        } else if (i - l !== 1) {
          pages.push(<Pagination.Ellipsis key={`ellipsis-${i}`} disabled />);
        }
      }
      pages.push(
        <Pagination.Item
          key={i}
          active={i - 1 === currentPage}
          onClick={() => onPageChange(i - 1)}
        >
          {i}
        </Pagination.Item>
      );
      l = i;
    });

    return pages;
  };

  return (
    <div>
      <div className={styles.advancedPagination}>
        <Pagination className="d-flex justify-content-center align-items-center flex-wrap">
          <Pagination.First
            onClick={() => onPageChange(0)}
            disabled={currentPage === 0}
          />
          <Pagination.Prev
            onClick={() => currentPage > 0 && onPageChange(currentPage - 1)}
          >
            <i className="bi bi-chevron-left"></i>
          </Pagination.Prev>
          {generatePages()}
          <Pagination.Next
            onClick={() =>
              currentPage + 1 < totalPages && onPageChange(currentPage + 1)
            }
          >
            <i className="bi bi-chevron-right"></i>
          </Pagination.Next>
          <Pagination.Last
            onClick={() => onPageChange(totalPages - 1)}
            disabled={currentPage + 1 === totalPages}
          />

          <div className={styles.pageSizeControl}>
            <label className="me-2 mb-0" htmlFor="pageSize">Page Size:</label>
            <Form.Select
              size="sm"
              value={pageSize}
              onChange={handlePageSizeChange}
              style={{ width: "auto" }}
            >
              <option value="10">10</option>
              <option value="25">25</option>
              <option value="50">50</option>
              <option value="100">100</option>
            </Form.Select>
          </div>
        </Pagination>

        <small className="text-muted mt-1 d-block text-center">
          Showing {currentPage * pageSize + 1}–
          {Math.min((currentPage + 1) * pageSize, totalItems)} of {totalItems}{" "}
          items
        </small>
      </div>
    </div>
  );
};

AdvancedPagination.propTypes = {
  currentPage: PropTypes.number.isRequired,
  totalPages: PropTypes.number.isRequired,
  pageSize: PropTypes.number.isRequired,
  handlePageSizeChange: PropTypes.func.isRequired,
  onPageChange: PropTypes.func.isRequired,
  totalItems: PropTypes.number.isRequired,
};

export default AdvancedPagination;
