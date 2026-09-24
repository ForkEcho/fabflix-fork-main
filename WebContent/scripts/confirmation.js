let orderData = JSON.parse(sessionStorage.getItem("OrderDetails"));
let orderTotal = sessionStorage.getItem("OrderTotal");

let orderHTML = `
    <p>Order ID(s): ${Array.isArray(orderData.id) ? orderData.id.join(", ") : orderData.id}</p>
    <p>Date: ${new Date(orderData.date).toLocaleString()}</p>
    <p>Total: $${orderTotal}</p>
`;

document.getElementById("order_details").innerHTML = orderHTML;
sessionStorage.removeItem("OrderDetails");
sessionStorage.removeItem("OrderTotal");