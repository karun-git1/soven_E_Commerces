// Highlights the selected payment method card on the checkout page.
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('input[name="paymentMethod"]').forEach(function (radio) {
        radio.addEventListener('change', function () {
            document.querySelectorAll('.payment-option').forEach(function (el) {
                el.classList.remove('border-brand');
            });
            var label = document.querySelector('label[for="' + radio.id + '"]');
            if (label) label.classList.add('border-brand');
        });
    });
});
