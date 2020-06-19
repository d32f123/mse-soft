let host = "http://localhost:8080";
let authUrl = host + "/api/v1/auth";
let dailyTasksUrl = host + "/api/v1/employees/daily-tasks";

let taskUrl = "task.html";

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

function createGroomerTable(token) {
    console.log("createGroomerTable(" + token + ") start");

    getTasks(token, function (tasks) {
        console.log(tasks);

        let tbody = document.getElementById("tbody");

        for (let task of tasks) {
            let tr = document.createElement("tr");

            let type = document.createElement("th");
            let a = document.createElement("a");
            a.innerText = task["taskType"];
            a.href = taskUrl + "?token=" + token + "&task=" + task['taskId'];
            type.appendChild(a);

            let timeStart = document.createElement("th");
            timeStart.innerText = task["scheduleEntry"]["timeStart"];

            let timeEnd = document.createElement("th");
            timeEnd.innerText = task["scheduleEntry"]["timeEnd"];

            tr.appendChild(type);
            tr.appendChild(timeStart);
            tr.appendChild(timeEnd);

            tbody.appendChild(tr);
        }
    });

    console.log("createGroomerTable() finish");
}
