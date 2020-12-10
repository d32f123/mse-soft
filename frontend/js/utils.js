let host = "http://localhost:8080";
let authUrl = host + "/api/v1/auth";
let getRoleUrl = host + "/api/v1/employees/get-role";
let dailyTasksUrl = host + "/api/v1/employees/daily-tasks";
let completeTaskUrl = host + "/api/v1/employees/complete-task";
let completeSubTaskUrl = host + "/api/v1/employees/complete-sub-task";

let taskUrl = "task.html";

function auth(data, success, error){
    $.ajax({
        type: "POST",
        url: authUrl,
        data: JSON.stringify(data),
        success: success,
        error: error,
        headers: {"Content-Type": "application/json"}
    });
}

function logout() {
    console.log("redirectToIndexPage() started");
    window.open("index.html","_self");
    console.log("redirectToindexPage() finished");
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
    console.log("redirectToGroomerPage(" + token + ") start");

    window.open("groomer.html?token="+token,"_self");

    console.log("redirectToGroomerPage finish");
}

function redirectToPigMasterPage(token) {
    console.log("redirectToPigMasterPage(" + token + ") start");

    window.open("pig_master.html?token=" + token, "_self");

    console.log("redirectToPigMasterPage finish");
}

function createGroomerTable(token) {
    console.log("createGroomerTable(" + token + ") start");

    getTasks(token, function (tasks) {
        console.log(tasks);

        let tbody = document.getElementById("tbody");

        for (let task of tasks) {
            let tr = document.createElement("tr");
            if (task.complete) {
                tr.classList.add("_complete");
            }

            let type = document.createElement("th");
            let a = document.createElement("a");
            a.innerText = task["taskType"];
            a.href = taskUrl + "?token=" + token + "&task=" + task['taskId'];
            type.appendChild(a);

            let timeStart = document.createElement("th");
            timeStart.innerText = getFormattedTime(task["scheduleEntry"]["timeStart"]);

            let timeEnd = document.createElement("th");
            timeEnd.innerText = getFormattedTime(task["scheduleEntry"]["timeEnd"]);

            tr.appendChild(type);
            tr.appendChild(timeStart);
            tr.appendChild(timeEnd);

            tbody.appendChild(tr);
        }
    });

    console.log("createGroomerTable() finish");
}

function createPigMasterTable(token) {
    console.log("createPigMasterTable(" + token + ") start");

    getTasks(token, function (tasks) {
        console.log(tasks);

        let tbody = document.getElementById("tbody");

        for (let task of tasks) {
            let tr = document.createElement("tr");
            if (task.complete) {
                tr.classList.add("_complete");
            }

            let type = document.createElement("th");
            let a = document.createElement("a");
            a.innerText = task["taskType"];
            a.href = taskUrl + "?token=" + token + "&task=" + task['taskId'];
            type.appendChild(a);

            let timeStart = document.createElement("th");
            timeStart.innerText = getFormattedTime(task["scheduleEntry"]["timeStart"]);

            let timeEnd = document.createElement("th");
            timeEnd.innerText = getFormattedTime(task["scheduleEntry"]["timeEnd"]);

            let pigstyNumber = document.createElement("th");
            let pigAmount = document.createElement("th");
            let lastFedTask = document.createElement("th");

            if (task['pigsty']) {
                pigstyNumber.innerText = task['pigsty']['pigstyNumber'];
                pigAmount.innerText = task['pigsty']['pigAmount'];
                lastFedTask.innerText = getFormattedDateTime(task['pigsty']['lastFeedTime']);
            }

            tr.appendChild(type);
            tr.appendChild(timeStart);
            tr.appendChild(timeEnd);
            tr.appendChild(pigstyNumber);
            tr.appendChild(pigAmount);
            tr.appendChild(lastFedTask);

            tbody.appendChild(tr);
        }
    });

    console.log("createPigMasterTable() finish");
}

// task page
function getTaskPageParams() {
    let token = window.location.search.substr(1).split("&")[0].split("=")[1];
    let taskId = window.location.search.substr(1).split("&")[1].split("=")[1];
    let dashboardUrl = document.referrer;
    console.log("token:\t" + token);
    console.log("taskId:\t" + taskId);

    return {token, taskId, dashboardUrl};
}

function getFormattedTime(timeString) {
    return new Date(timeString).toLocaleTimeString('ru-RU');
}

function getFormattedDateTime(dateTimeString) {
    return new Date(dateTimeString).toLocaleString('ru-RU');
}

function setUpTaskPage(token, taskId, dashboardUrl) {
    console.log("loadTaskInfo(" + token + "," + taskId + ") start");

    getTasks(token, function (tasks) {
        console.log(tasks);

        let curTask = null;

        for (let task of tasks) {
            if (task["taskId"] === taskId) {
                curTask = task;
                break;
            }
        }

        // show task completing
        document.querySelector("h1").innerText = curTask['taskType'];
        if (curTask["complete"]) {
            showTaskComplete();
        }

        // show task info
        document.getElementById("status").innerText = curTask['body']["state"];
        document.getElementById("barcode").innerText = curTask['body']["barcode"];
        document.getElementById("planed_time").innerText = getFormattedTime(curTask["scheduleEntry"]["timeStart"]);
        const dashboard = new URL(dashboardUrl);
        document.getElementById("schedule").setAttribute("href", `${dashboard.pathname}?token=${token}`);

        let actions = document.getElementById("actions")

        for (let subTask of curTask["subTasks"]) {
            let div = document.createElement("div");
            div.setAttribute("class", "form-check mb-2");

            let input = document.createElement("input");
            input.setAttribute("class", "form-check-input");
            input.setAttribute("type", "checkbox");
            input.setAttribute("class", "form-check-input");
            input.setAttribute("value", subTask["subTaskType"]);
            input.setAttribute("id", subTask["subTaskId"]);
            if (subTask['complete']) {
                input.setAttribute("disabled", "disabled");
            }

            let label = document.createElement("label");
            label.setAttribute("class", "form-check-label");
            label.setAttribute("for", subTask["subTaskType"]);
            label.innerText = subTask["subTaskType"];

            div.appendChild(input);
            div.appendChild(label);
            actions.appendChild(div);
        }
    });

    console.log("loadTaskInfo() finish");
}

function completeSubTask(token, subTaskId) {
    console.log("completeSubTask(" + token + ", " + subTaskId + ") start");

    $.ajax({
        type: "POST",
        url: completeSubTaskUrl + "/" + subTaskId,
        dataType: "json",
        headers: {Token: token},
        success: function (response) {
            let input = document.getElementById(subTaskId);
            input.setAttribute("disabled","disabled");

            console.log("completeSubTask(" + token + ", " + subTaskId + ") finish");
        },
    });
}

function showTaskComplete() {
    document.getElementById("bodyTitle").setAttribute(
        "class", "alert alert-success"
    );

    document.getElementById("btnCompleteTask").setAttribute("disabled", "disabled");
    document.getElementById("btnCompleteSubTask").setAttribute("disabled", "disabled");
}

function completeSubTasks() {
    console.log("completeSubTasks() start");

    let actions = document.getElementById("actions");
    let params = getTaskPageParams();
    let checkedSubTasks = null;

    // get checked actions
    for (let actionDiv of actions.querySelectorAll("div")) {
        let input = actionDiv.querySelector("input");

        if (input.checked) {
            completeSubTask(params["token"], input.getAttribute("id"));
        }
    }

    console.log("completeSubTasks() finish");
}

function completeTask() {
    console.log("completeTask() start");

    let params = getTaskPageParams();
    let taskId = params['taskId'];
    let token = params['token'];

    $.ajax({
        type: "POST",
        url: completeTaskUrl + "/" + taskId,
        dataType: "json",
        headers: {Token: token},
        success: function (response) {
            showTaskComplete();
            console.log("completeTask(" + token + ", " + taskId + ") finish");
        },
    });

    console.log("completeTask() finish");
}

function getRoleSync(token, success, error) {
    console.log("getRole() started");
    $.ajax({
        async: false,
        type: "GET",
        url: getRoleUrl,
        success: success,
        error: error,
        dataType: "json",
        headers: {Token: token}
    });
    console.log("getRole() finished");
}

function getRole(token) {
    console.log("getRole() start");
    getRole.role = null;

    getRoleSync(token, function (response) {
        console.log("Response: " + response);
        if (response.length > 0) {
            getRole.role = response;
        }
    }, null);

    console.log("getRole() -> " + getRole.role);
    return getRole.role;
}
