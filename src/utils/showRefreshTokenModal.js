let showModalExternal = null;

export const registerShowModal = (fn) => {
  showModalExternal = fn;
};

export const showRefreshTokenModal = () => {
  if (showModalExternal) {
    showModalExternal();
  } else {
    console.warn("Modal not yet mounted.");
  }
};