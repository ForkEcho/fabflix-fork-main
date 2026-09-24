/**
 * Handle the data returned by CartServlet
 * @param resultDataString jsonObject, consists of session info
 */
function handleSessionData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle session response");
    console.log(resultDataJson);

    // show cart information
    handleCartArray(resultDataJson["cartItems"]);
    jQuery("#cart_total").text(`Total: $${resultDataJson["total"]}`);
    sessionStorage.setItem("OrderTotal", resultDataJson["total"].toFixed(2));

}

/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultArray) {
    console.log(resultArray);
    let item_list = jQuery("#cart_items_body");

    item_list.empty();
    // change it to html list
    for (let i = 0; i < resultArray.length; i++) {
        const item = resultArray[i];

        let rowHTML = "";
        rowHTML += `<tr data-item_id="${item["id"]}">`;
        rowHTML += `<th><a href="./single-movie.html?id=${item["id"]}">` + item["title"] + "</a></th>";
        rowHTML += "<th>" + item["price"] + "</th>";
        rowHTML += "<th>" + item["quantity"] + "</th>";
        rowHTML += "<th>" + item["total"].toFixed(2) + "</th>";
        rowHTML += `<th>
                            <button type="button" class="btn btn-sm btn-primary cart_action" data-action="decrement">-</button>
                            <button type="button" class="btn btn-sm btn-primary cart_action" data-action="increment">+</button>
                            <button type="button" class="btn btn-sm btn-primary cart_action" data-action="remove">Remove</button>
                    </th>`;
        rowHTML += "</tr>";

        item_list.append(rowHTML);
    }
}


function handleCartInfo(itemId, action) {
    console.log("change cart");
    $.ajax("api/cart", {
        method: "POST",
        data: {item: itemId, action: action},
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);
            handleCartArray(resultDataJson["cartItems"]);
            sessionStorage.setItem("OrderTotal", resultDataJson["total"].toFixed(2));
            jQuery("#cart_total").text(`Total: $${resultDataJson["total"].toFixed(2)}`);
        }
    });
}

jQuery("#cart_items_body").on("click", ".cart_action", function () {
    let action = jQuery(this).data("action");
    let itemId = jQuery(this).closest("tr").data("item_id");
    handleCartInfo(itemId, action);
});


$.ajax("api/cart", {
    method: "GET",
    success: handleSessionData
});
