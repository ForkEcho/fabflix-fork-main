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

const dropdownSortingData = [
    {
        "label": "Rating Desc (Default)",
    },
    {
        "label": "Rating Asc, Title Desc",
        "sort-primary": "rating-asc",
        "sort-secondary": "title-desc",
    },
    {
        "label": "Rating Asc, Title Asc",
        "sort-primary": "rating-asc",
        "sort-secondary": "title-asc",
    },
    {
        "label": "Rating Desc, Title Asc",
        "sort-primary": "rating-desc",
        "sort-secondary": "title-asc",
    },
    {
        "label": "Rating Desc, Title Desc",
        "sort-primary": "rating-desc",
        "sort-secondary": "title-desc",
    },
    {
        "label": "Title Asc, Rating Desc",
        "sort-primary": "title-asc",
        "sort-secondary": "rating-desc",
    },
    {
        "label": "Title Asc, Rating Asc",
        "sort-primary": "title-asc",
        "sort-secondary": "rating-asc",
    },
    {
        "label": "Title Desc, Rating Asc",
        "sort-primary": "title-desc",
        "sort-secondary": "rating-asc",
    },
    {
        "label": "Title Desc, Rating Desc",
        "sort-primary": "title-desc",
        "sort-secondary": "rating-desc",
    },
]

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

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
function getCurrentSortingMethodIndex() {
    const url = new URL(window.location.href);
    const primaryParam = url.searchParams.get('sort-primary');
    const secondaryParam = url.searchParams.get('sort-secondary');

    if (!primaryParam && !secondaryParam) {
        return 0;
    } else {
        for (let i = 1; i < dropdownSortingData.length; i++) {
            if (dropdownSortingData[i]["sort-primary"] == primaryParam && dropdownSortingData[i]["sort-secondary"] == secondaryParam) {
                return i;
            }
        }
    }

    return 0;
}


function saveUrlParams() {
    sessionStorage.clear();
    saveParamIfNotNull("N", getParameterByName("N"));
    saveParamIfNotNull("browse-title", getParameterByName("browse-title"));
    saveParamIfNotNull("title", getParameterByName("title"));
    saveParamIfNotNull("genre", getParameterByName("genre"));
    saveParamIfNotNull("director", getParameterByName("director"));
    saveParamIfNotNull("year", getParameterByName("year"));
    saveParamIfNotNull("star", getParameterByName("star"));
    saveParamIfNotNull("offset", getParameterByName("offset"));
    saveParamIfNotNull("sort-primary", getParameterByName("sort-primary"));
    saveParamIfNotNull("sort-secondary", getParameterByName("sort-secondary"));
}

function saveParamIfNotNull(param, paramValue) {
    if (paramValue != null) {
        sessionStorage.setItem(param, paramValue)
    }
}

function handleResult(resultData) {

    console.log("handleResult: populating movie table from resultData");
    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    saveUrlParams();
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += `<tr data-movie_id="${resultData[i]["movie_id"]}">`;
        rowHTML += `<th><a href="./single-movie.html?id=${resultData[i]["movie_id"]}">${resultData[i]["movie_title"]}</a></th>`;
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += renderColumn(resultData[i]["genre_id"], resultData[i]["genre_name"], "./movie-list.html?genre=");
        rowHTML += renderColumn(resultData[i]["star_id"], resultData[i]["star_name"], "./single-star.html?id=");
        rowHTML += `<th>${resultData[i]["movie_rating"]}</th>`;
        rowHTML += `<th>
                        <button type="button" class="btn btn-sm btn-primary cart_action" data-id="${resultData[i]["movie_id"]}"> Add to Cart</button>
                    </th>`;
        rowHTML += "</tr>";

        movieTableBodyElement.append(rowHTML);
    }

    const offset = getParameterByName("offset");
    const n = getParameterByName("N");

    /* Dropdown Pagination Logic */
    const dropdownRecordsElement = jQuery("#dropdown-pagination");
    const nDropdownValues = [10, 25, 50, 100]
    for (let i = 0; i < nDropdownValues.length; i++) {
        dropdownRecordsElement.append(`<option value="${nDropdownValues[i]}">${nDropdownValues[i]}</option>`)
    }

    dropdownRecordsElement.change(dropdownNOnSelect);
    let divPageNavElement = jQuery("#page-nav");
    const currentURL = new URL(window.location.href);

    var offsetValue = 0;
    var nValue = 25;

    if (n != null && !n.empty) {
        nValue = Number.parseInt(n);
    }

    dropdownRecordsElement.val(nValue);
    if (offset != null && !offset.empty) {
        offsetValue = Number.parseInt(offset);
        const prevURL = currentURL;
        prevURL.searchParams.set('offset', offsetValue - nValue);
        divPageNavElement.append(`<a href="${prevURL}">Prev</a>`);
    }

    if (resultData.length != 0) {
        const nextURL = currentURL;
        nextURL.searchParams.set('offset', offsetValue + nValue);
        divPageNavElement.append(`<a href="${nextURL}">Next</a>`);
    }

    /* Dropdown Sorting Labels Logic */
    const dropdownSortingElement = jQuery("#dropdown-sorting");
    console.log(dropdownSortingElement.length);
    for (let i = 0; i < dropdownSortingData.length; i++) {
        dropdownSortingElement.append(`<option value="${i}">${dropdownSortingData[i].label}</option>`)
    }

    dropdownSortingElement.change(dropdownSortingMethodOnSelect);
    dropdownSortingElement.val(getCurrentSortingMethodIndex());
    dropdownSortingElement.visible = true;
}

function dropdownNOnSelect() {
    const nValue = this.value;
    const url = new URL(window.location.href);
    url.searchParams.set('N', nValue);
    window.location.assign(url);
}

function dropdownSortingMethodOnSelect() {
    const sortingIndex = this.value;
    const url = new URL(window.location.href);

    if (sortingIndex == 0) {
        url.searchParams.delete('sort-primary');
        url.searchParams.delete('sort-secondary');
    } else {
        const data = dropdownSortingData[sortingIndex]
        url.searchParams.set('sort-primary', data['sort-primary'])
        url.searchParams.set('sort-secondary', data['sort-secondary'])
    }
    window.location.assign(url);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Makes the HTTP GET request and registers on success callback function handleResult
const params = new URLSearchParams(window.location.search);

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/movie-list?" + params,
    success: (resultData) => handleResult(resultData)
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