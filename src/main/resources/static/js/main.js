// Shared front-end behaviour for the storefront.
document.addEventListener('DOMContentLoaded', function () {
    // Auto-dismiss alert banners after a few seconds.
    document.querySelectorAll('.alert-auto-dismiss').forEach(function (alertEl) {
        setTimeout(function () {
            alertEl.classList.add('d-none');
        }, 4000);
    });
});
