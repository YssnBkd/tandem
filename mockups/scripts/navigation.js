/**
 * Tandem Mockups - Navigation Helper
 * Provides simple navigation between mockup screens
 */

// Navigate to a screen
function navigateTo(screen) {
  window.location.href = screen;
}

// Initialize navigation from data-nav attributes
function initNavigation() {
  document.querySelectorAll('[data-nav]').forEach(el => {
    el.style.cursor = 'pointer';
    el.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();
      const destination = el.getAttribute('data-nav');
      if (destination) {
        navigateTo(destination);
      }
    });
  });
}

// Run on DOM ready
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initNavigation);
} else {
  initNavigation();
}
