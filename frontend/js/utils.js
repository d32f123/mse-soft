let host = "http://localhost:8080";
let authUrl = host + "/api/v1/auth";
let dailyTasksUrl = host + "/api/v1/employees/daily-tasks";

function auth(data, success, error){
    $.ajax({
        type: "GET",
        url: authUrl,
        data: data,
        success: success,
        error: error,
        dataType: "text"
    });
}

function getTasks(token, success, error) {
    $.ajax({
        type: "GET",
        url: dailyTasksUrl,
        success: success,
        error: error,
        dataType: "json",
        headers: {Token: token}
    });
}

function redirectToGroomerPage(token) {
    console.log("setUpGroomerPage(" + token + ") start");

    window.open("groomer.html?token="+token,"_self");

    console.log("setUpGroomerPage finish");
}
