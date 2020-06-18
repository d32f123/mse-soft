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