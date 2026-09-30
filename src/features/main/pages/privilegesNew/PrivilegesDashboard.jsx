import { useEffect } from 'react';
import { Nav } from 'react-bootstrap';
import { Link, useLocation, useNavigate } from 'react-router-dom';

export const PrivilegesDashboard = () => {
  const location = useLocation();
  const navigate = useNavigate();

  // Step configuration for the navigation flow in privileges module.
  // Each step represents a separate management screen.
  const steps = [
    { path: '/view-all-privileges', label: '1. Privileges' },
    { path: '/view-all-permissions', label: '2. Permissions' },
    { path: '/view-all-endpoints', label: '3. End Points' },
  ];

  // Returns style for each step block.
  // Highlights the current step by changing background and text color.
  const getStepStyle = (path) => ({
    flex: 1,
    height: '50px',
    textAlign: 'center',
    padding: '10px 0',
    backgroundColor:
      location.pathname === path ? 'rgb(126 115 228)' : '#bebefc',
    color: location.pathname === path ? '#ffffff' : '#000000',
    fontWeight: 'bold',
    position: 'relative',
    cursor: 'pointer',
  });

  // Returns style for the arrow connector between step blocks.
  // Arrow inherits color based on which step is currently active.
  const getArrowStyle = (index) => ({
    height: '50px',
    position: 'absolute',
    right: -20,
    top: 0,
    width: 0,
    borderTop: '25px solid transparent',
    borderBottom: '25px solid transparent',
    borderLeft: `20px solid ${location.pathname === steps[index].path
      ? 'rgb(126 115 228)'
      : '#bebefc'
      }`,
    zIndex: 1,
  });

  // Redirect to default step if none is selected
  useEffect(() => {
    if (!steps.some((step) => step.path === location.pathname)) {
      navigate('/view-all-privileges', { replace: true });
    }
  }, [location.pathname, navigate, steps]);

  return (
    <div style={{ display: 'flex', alignItems: 'center', margin: '20px 0' }}>
      {steps.map((step, index) => (
        <div
          key={step.path}
          style={{ display: 'flex', flex: 1, position: 'relative' }}
        >
          <Nav.Link as={Link} to={step.path} style={getStepStyle(step.path)}>
            {step.label}
          </Nav.Link>
          {index < steps.length - 1 && <div style={getArrowStyle(index)} />}
        </div>
      ))}
    </div>
  );
};
