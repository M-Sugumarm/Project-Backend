const axios = require('axios');

async function simulateOrder() {
    try {
        // 1. Get products to get a valid product ID
        const productsRes = await axios.get('http://localhost:8080/api/products');
        const products = productsRes.data;
        if (products.length === 0) {
            console.log("No products found to order.");
            return;
        }
        const product = products[0];

        // 2. We bypass the cart for this test and use the direct items endpoint 
        // Wait, let's see if we can use the order/direct-items endpoint or just add to cart.
        // OrderController has: public ResponseEntity<Order> createOrder(@RequestBody Map<String, Object> requestData)
        // requestData ex: { userId, shippingAddress, stripePaymentId }

        // Let's just create an order directly
        const orderData = {
            userId: "test-user-sim",
            shippingAddress: "123 Sim Lane",
            stripePaymentId: "pi_sim_" + Date.now(),
            items: [
                {
                    id: product.id,
                    quantity: 1,
                    size: "M",
                    color: "Red"
                }
            ]
        };

        // Check if there is a createOrderFromItems endpoint
        const res = await axios.post('http://localhost:8080/api/orders/direct', orderData);
        console.log("Order simulated successfully:", res.data);

    } catch (error) {
        console.error("Error simulating order:", error.response ? error.response.data : error.message);
    }
}

simulateOrder();
