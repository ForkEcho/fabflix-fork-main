/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */

function renderColumn(ids, names, link = null) {
    let rowHTML = "";
    let idsArray = ids.split(",");
    let nameArray = names.split(",");
    rowHTML += `<th>`;
    for (let i = 0; i < Math.min(3, idsArray.length); i++) {
        if (link) {
            rowHTML += `<p><a href="${link}${idsArray[i]}">${nameArray[i]}</a></p>`;
        } else {
            rowHTML += `<p>${nameArray[i]}</p>`;
        }
    }
    rowHTML += `</th>`;
    return rowHTML;
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating movie table from resultData");
    console.log(resultData)
    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < Math.min(20, resultData.length); i++) {
        let rowHTML = "";
        rowHTML += `<tr data-movie_id="${resultData[i]["movie_id"]}">`;
        rowHTML += `<th><a href="./single-movie.html?id=${resultData[i]["movie_id"]}">${resultData[i]["movie_title"]}</a></th>`;
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += renderColumn(resultData[i]["genre_id"], resultData[i]["genre_name"]);
        rowHTML += renderColumn(resultData[i]["star_id"], resultData[i]["star_name"], "./single-star.html?id=");
        rowHTML += `<th>${resultData[i]["movie_rating"]}</th>`;
        rowHTML += `<th>
                        <button type="button" class="btn btn-sm btn-primary cart_action" data-id="${resultData[i]["movie_id"]}"> Add to Cart</button>
                    </th>`;
        rowHTML += "</tr>";

        movieTableBodyElement.append(rowHTML);
    }
}

let searchForm = $("#input-div")
function submitSearch(formSubmissionEvent) {
    console.log("Submitting Search...");
    formSubmissionEvent.preventDefault();
    const params = new URLSearchParams(searchForm.serialize());
    window.location.assign("./movie-list.html?" + params);
}

searchForm.submit(submitSearch);

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/movie-list", success: (resultData) => handleResult(resultData)
});

jQuery(document).on("click", ".cart_action", function () {
    const movieId = jQuery(this).data("id");
    $.ajax("api/cart", {
        method: "POST",
        data: {item: movieId, action: "add"},
        success: function () {
            alert("Added to cart!");
        },
        error: function () {
            alert("Failed to add to cart.");
        }
    });

});