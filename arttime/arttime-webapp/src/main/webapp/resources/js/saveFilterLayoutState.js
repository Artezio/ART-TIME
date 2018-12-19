var filterCollapsed = localStorage.getItem('filterCollapsed');

window.onFilterOpen = document.onFilterOpen || function() {
    localStorage.setItem('filterCollapsed', 'false');
};

window.onFilterClose = document.onFilterClose || function() {
    localStorage.setItem('filterCollapsed', 'true');
};

window.isPanelOpen = document.isPanelOpen || function() {
    return !(localStorage.getItem('filterCollapsed') === 'true');
};

