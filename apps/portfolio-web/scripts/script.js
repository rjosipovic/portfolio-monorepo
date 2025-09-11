// Smooth scrolling for navigation links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const href = this.getAttribute('href');
        // Check if the href is just "#" or empty
        if (href === '#' || href === '') {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        } else {
            const targetElement = document.querySelector(href);
            if (targetElement) { // Ensure the target element exists
                targetElement.scrollIntoView({
                    behavior: 'smooth'
                });
            } else {
                console.warn(`Target element for href="${href}" not found.`);
            }
        }
    });
});

// Mobile menu toggle functionality
const mobileMenuButton = document.getElementById('mobile-menu-button');
const closeMobileMenuButton = document.getElementById('close-mobile-menu');
const mobileMenu = document.getElementById('mobile-menu');

function toggleMobileMenu() {
    mobileMenu.classList.toggle('hidden');
    mobileMenu.classList.toggle('flex');
}

mobileMenuButton.addEventListener('click', toggleMobileMenu);
closeMobileMenuButton.addEventListener('click', toggleMobileMenu);

// Close mobile menu when a link is clicked
mobileMenu.querySelectorAll('a').forEach(link => {
    link.addEventListener('click', toggleMobileMenu);
});

// Add a subtle animation to the hero section elements on load
document.addEventListener('DOMContentLoaded', () => {
    const heroElements = document.querySelectorAll('.animate-fade-in-up');
    heroElements.forEach((el) => {
        // Trigger reflow to ensure animation starts from initial state
        void el.offsetWidth;
        el.style.animationPlayState = 'running';
    });
});