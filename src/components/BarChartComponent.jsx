import React from "react";
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, CartesianGrid, Cell } from "recharts";
import PropTypes from "prop-types";

// Predefined list of colors used for each bar segment.
// If data contains more items than the COLORS list, the colors repeat.
const COLORS = [
    "#1abc9c", "#3498db", "#9b59b6", "#e67e22", "#2ecc71",
    "#e74c3c", "#34495e", "#16a085", "#f39c12", "#8e44ad"
];

/**
 * BarChartComponent
 * ------------------
 * A reusable wrapper around Recharts BarChart.
 * 
 * Props:
 * - data: Array of objects representing chart data.
 * - dataKey: Field name used for the bar values.
 * - nameKey: Field name used for the X-axis labels.
 *
 * Features:
 * - Responsive bar colors with fallback rotation.
 * - Tooltip showing values with "Currency" suffix.
 * - Legend for bar identification.
 * - Grid for easier visualization.
 */
const BarChartComponent = ({ data, dataKey, nameKey }) => {
    return (
        <div style={{ textAlign: "center" }}>
            <BarChart width={900} height={300} data={data} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey={nameKey} />
                <YAxis />
                <Tooltip formatter={(value) => [`${value} Currency`]} />
                <Legend />
                <Bar dataKey={dataKey} name="Currency" >
                    {data.map((entry, index) => (
                        <Cell key={entry.id} fill={COLORS[index % COLORS.length]} />
                    ))}
                </Bar>
            </BarChart>
        </div>
    );
};

// Add prop types validation 
BarChartComponent.propTypes = {
    data: PropTypes.arrayOf(PropTypes.object).isRequired,
    dataKey: PropTypes.string.isRequired,
    nameKey: PropTypes.string.isRequired
};

export default BarChartComponent;
