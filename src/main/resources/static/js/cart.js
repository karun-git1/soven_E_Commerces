// Cart quantity stepper helper: keeps the number input in sync with +/- buttons
// before the form (server-rendered) submits the update.
function stepQuantity(inputId, delta) {
    var input = document.getElementById(inputId);
    if (!input) return;
    var value = parseInt(input.value || '1', 10) + delta;
    if (value < 0) value = 0;
    input.value = value;
}
