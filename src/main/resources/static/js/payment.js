document.addEventListener('DOMContentLoaded', function () {
	var form = document.querySelector('form[action*="/checkout/place-order"]');
	if (!form || !window.Razorpay) return;

	form.addEventListener('submit', async function (event) {
		event.preventDefault();
		var button = form.querySelector('button[type="submit"]');
		button.disabled = true;
		try {
			var response = await fetch('/payment/razorpay/order', {
				method: 'POST',
				body: new FormData(form)
			});
			var responseText = await response.text();
			var orderData;
			try {
				orderData = JSON.parse(responseText);
			} catch (parseError) {
				throw new Error('Payment service returned an unexpected response. Check the server configuration.');
			}
			if (!response.ok) throw new Error(orderData.message || 'Could not start payment');

			var checkout = new Razorpay({
				key: orderData.keyId,
				amount: orderData.amount,
				currency: orderData.currency,
				name: 'ShopEase',
				description: 'E-Commerce Purchase',
				order_id: orderData.razorpayOrderId,
				handler: async function (result) {
					var verifyResponse = await fetch('/payment/razorpay/verify', {
						method: 'POST',
						headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
						body: new URLSearchParams({
							orderId: orderData.orderId,
							razorpayOrderId: result.razorpay_order_id,
							razorpayPaymentId: result.razorpay_payment_id,
							razorpaySignature: result.razorpay_signature
						})
					});
					var verification = await verifyResponse.json();
					if (!verifyResponse.ok) throw new Error(verification.message || 'Payment verification failed');
					window.location.href = verification.redirectUrl;
				},
				modal: { ondismiss: function () { button.disabled = false; } }
			});
			checkout.on('payment.failed', function () {
				button.disabled = false;
				window.alert('Payment failed. Please try again.');
			});
			checkout.open();
		} catch (error) {
			button.disabled = false;
			window.alert(error.message);
		}
	});
});
