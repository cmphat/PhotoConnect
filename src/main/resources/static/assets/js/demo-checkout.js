(function () {
    'use strict';

    function initializeCheckout() {
        var form = document.getElementById('demo-payment-form');
        if (!form) return;

        var methodInputs = Array.prototype.slice.call(form.querySelectorAll('input[name="paymentMethod"]'));
        var panels = Array.prototype.slice.call(form.querySelectorAll('[data-payment-panel]'));
        var cardFields = Array.prototype.slice.call(form.querySelectorAll('[data-card-field]'));
        var cardNumber = form.querySelector('[data-card-number]');
        var overlay = document.querySelector('[data-processing-overlay]');
        var submitButton = form.querySelector('[data-submit-payment]');

        function selectedMethod() {
            var selected = methodInputs.find(function (input) { return input.checked; });
            return selected ? selected.value : '';
        }

        function updateMethod() {
            var method = selectedMethod();
            panels.forEach(function (panel) {
                panel.hidden = panel.getAttribute('data-payment-panel') !== method;
            });
            cardFields.forEach(function (field) {
                var cardSelected = method === 'DEMO_CARD';
                field.disabled = !cardSelected;
                field.required = cardSelected;
            });
        }

        methodInputs.forEach(function (input) {
            input.addEventListener('change', updateMethod);
        });

        if (cardNumber) {
            cardNumber.addEventListener('input', function () {
                var digits = cardNumber.value.replace(/\D/g, '').slice(0, 16);
                cardNumber.value = digits.replace(/(.{4})/g, '$1 ').trim();
            });
        }

        form.addEventListener('submit', function (event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                form.reportValidity();
                return;
            }
            event.preventDefault();
            submitButton.disabled = true;
            submitButton.setAttribute('aria-busy', 'true');
            if (overlay) overlay.hidden = false;
            window.setTimeout(function () { form.submit(); }, 550);
        });

        updateMethod();
    }

    document.addEventListener('DOMContentLoaded', initializeCheckout);
}());
