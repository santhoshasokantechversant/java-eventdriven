import React from "react";
import { Pie, Cell, Tooltip, Legend, PieChart, ResponsiveContainer } from "recharts";
import PropTypes from "prop-types";

/**
 * PieChartComponent
 * ------------------
 * Reusable Pie chart component using Recharts.
 *
 * Props:
 * - data: Array of objects representing chart data.
 * - dataKey: Field name to use for numeric values.
 * - nameKey: Field name to use for labels/categories.
 *
 * Features:
 * - Auto-responsive using ResponsiveContainer.
 * - Custom labels displayed inside the chart slices.
 * - Tooltip + Legend for improved readability.
 * - Dynamically assigned colors for slices.
 */

const COLORS = ["teal", "#b93e3e", "#34495e", "#8e44ad", "#27ae60", "#2980b9"];

const PieChartComponent = ({ data, dataKey, nameKey }) => {
        /**
     * Custom label renderer for displaying values inside each slice.
     * Calculates label position based on slice geometry.
     */
    const renderCustomizedLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, value }) => {
        const RADIAN = Math.PI / 180;
        const radius = innerRadius + (outerRadius - innerRadius) / 2;
        const x = cx + radius * Math.cos(-midAngle * RADIAN);
        const y = cy + radius * Math.sin(-midAngle * RADIAN);

        return (
            <text
                x={x}
                y={y}
                fill="white"
                textAnchor="middle"
                dominantBaseline="central"
                fontSize={14}
                fontWeight="bold"
            >
                {value}
            </text>
        );
    };

    return (
        <div style={{ width: "100%", height: 300, textAlign: "center" }}>
            <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                    <Pie
                        data={data}
                        cx="50%"
                        cy="50%"
                        outerRadius="80%"
                        dataKey={dataKey}
                        nameKey={nameKey}
                        label={renderCustomizedLabel}
                        labelLine={false}
                    >
                        {data.map((entry, index) => (
                            <Cell key={entry.id} fill={COLORS[index % COLORS.length]} />
                        ))}
                    </Pie>
                    <Tooltip
                        formatter={(value, name) => [`${value} Users`, name]}
                        contentStyle={{ fontSize: '12px', padding: '2px 4px', borderRadius: '4px' }}
                        itemStyle={{ color: '#000' }}
                    />
                    <Legend />
                </PieChart>
            </ResponsiveContainer>
        </div>
    );
};

// Add prop types validation 
PieChartComponent.propTypes = {
    data: PropTypes.arrayOf(PropTypes.object).isRequired,
    dataKey: PropTypes.string.isRequired,
    nameKey: PropTypes.string.isRequired,
};

export default PieChartComponent;
