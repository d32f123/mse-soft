let port = 20755;
let hostname = "http://localhost:";
let host = hostname + port;
let authUrl = host + "/api/v1/auth";
let getRoleUrl = host + "/api/v1/employees/get-role";
let dailyTasksUrl = host + "/api/v1/employees/daily-tasks";
let completeTaskUrl = host + "/api/v1/employees/complete-task";
let completeSubTaskUrl = host + "/api/v1/employees/complete-sub-task";
let orderUrl = host + "/order-status"
let payUrl = host + "/api/v1/hydra/orders"
let token;
let taskUrl = "task.html";
var userName;
redirectToCustomerPage

function auth(data, success, error) {
  userName = data.username;
  $.ajax({
    type: "POST",
    url: authUrl,
    data: JSON.stringify(data),
    success: success,
    error: error,
    headers: {"Content-Type": "application/json"}
  });
}

function getBodyStatus() {
  let paymentId = document.getElementById("paymentIdGet").value;
  $.ajax({
    type: "GET",
    url: orderUrl + "?paymentId=" + paymentId,
    headers: {"Content-Type": "application/json"},
    success: function (response) {
      document.getElementById("amountGet").value = response;
    },
  });
}

function createOrder() {
  $.ajax({
    type: "POST",
    url: orderUrl,
    headers: {"Content-Type": "application/json"},
    success: function (response) {
      document.getElementById("paymentIdPost").value = response.paymentId;
      document.getElementById("amountPost").value = response.paymentAmount;
    }
  });
}

function payForOrder0() {
  let paymentId = document.getElementById("paymentIdPut").value;
  let amount = 0;
  let data = {
    "paymentId": paymentId,
    "amount": amount
  };
  $.ajax({
    type: "PUT",
    url: payUrl,
    data: JSON.stringify(data),
    headers: {"Content-Type": "application/json"},
    success: function (response) {
      let amount = response.paymentAmount;
      document.getElementById("paymentIdGet").value = response.paymentId;
      document.getElementById("amountToBe").value = amount;
      if (amount == 0) {
        document.getElementById("getBodyStatus").disabled = false;
      } else {
        document.getElementById("getBodyStatus").disabled = true;
      }

      document.getElementById("error").innerHTML = ""
    },
    error: function (response) {
      document.getElementById("error").innerHTML = "Ошибка при оплате или получении данных о заказе. Введены некорректные данные."
    }
  });

}

function payForOrder() {
  let paymentId = document.getElementById("paymentIdPut").value;
  let amount = document.getElementById("amountPut").value;
  let data = {
    "paymentId": paymentId,
    "amount": amount
  };
  $.ajax({
    type: "PUT",
    url: payUrl,
    data: JSON.stringify(data),
    headers: {"Content-Type": "application/json"},
    success: function (response) {
      let amount = response.paymentAmount;
      document.getElementById("paymentIdGet").value = response.paymentId;
      document.getElementById("amountToBe").value = amount;
      if (amount == 0) {
        document.getElementById("getBodyStatus").disabled = false;
      } else {
        document.getElementById("getBodyStatus").disabled = true;
      }
      document.getElementById("error").innerHTML = ""
    },
    error: function (response) {
      document.getElementById("error").innerHTML = "Ошибка при оплате. Введены некорректные данные."
    }
  });

}

function getTokenFromUrl() {
  let token = window.location.search.substr(1).split("=")[1];
  token = token.split("&")[0];

  console.log("token: " + token);
  return token;
}

function getNameFromUrl() {
  let token = window.location.search.substr(1).split("=")[1];
  token = token.split("&")[0];

  console.log("token: " + token);
  return token;
}

function updatePage() {
  window.setTimeout(function () {
    let url = window.location;
    window.open(url, "_self");
  }, 300);
}

function logout() {
  console.log("redirectToIndexPage() started");
  $.ajax({
    type: "DELETE",
    url: authUrl,
    dataType: "json",
    headers: {Token: token}
  });
  redirectToIndexPage();
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

function changePort() {
  port = document.getElementById("portInput").value;
  updateUrls();
}

function updateUrls() {
  host = hostname + port;
  authUrl = host + "/api/v1/auth";
  getRoleUrl = host + "/api/v1/employees/get-role";
  dailyTasksUrl = host + "/api/v1/employees/daily-tasks";
  completeTaskUrl = host + "/api/v1/employees/complete-task";
  completeSubTaskUrl = host + "/api/v1/employees/complete-sub-task";
}

function redirectToGroomerPage(token) {
  console.log("redirectToGroomerPage(" + token + ") start");

  window.open("groomer.html?token=" + token + "&username=" + userName, "_self");

  console.log("redirectToGroomerPage finish");
}

function redirectToPigMasterPage(token) {
  console.log("redirectToPigMasterPage(" + token + ") start");

  window.open("pig_master.html?token=" + token, "_self");

  console.log("redirectToPigMasterPage finish");
}

function redirectToPortPage() {
  console.log("redirectToPortPage start");

  window.open("port.html", "_self");

  console.log("redirectToPortPage finish");
}

function redirectToCustomerPage() {
  console.log("redirectToCustomerPage start");

  window.open("customer.html", "_self");

  console.log("redirectToCustomerPage finish");
}

function redirectToIndexPage() {
  console.log("redirectToIndexPage start");

  window.open("index.html", "_self");

  console.log("redirectToIndexPage finish");
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
      a.setAttribute("name", task["taskType"]);
      type.appendChild(a);

      let timeStart = document.createElement("th");
      timeStart.innerText = getFormattedTime(
          task["scheduleEntry"]["timeStart"]);

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
    let possible = ["IN_FEEDING", "FED", "GROOMED"];
    for (let task of tasks) {
      let tr = document.createElement("tr");
      if (task.complete) {
        tr.classList.add("_complete");
      }

      let type = document.createElement("th");
      let a = document.createElement("a");
      a.innerText = task["taskType"];
      a.href = taskUrl + "?token=" + token + "&task=" + task['taskId'];
      a.setAttribute("name", task["taskType"]);
      type.appendChild(a);

      let timeStart = document.createElement("th");
      timeStart.innerText = getFormattedTime(
          task["scheduleEntry"]["timeStart"]);

      let timeEnd = document.createElement("th");
      timeEnd.innerText = getFormattedTime(task["scheduleEntry"]["timeEnd"]);

      let pigstyNumber = document.createElement("th");
      let pigAmount = document.createElement("th");
      let lastFedTask = document.createElement("th");

      if (task['pigsty']) {
        pigstyNumber.innerText = task['pigsty']['pigstyNumber'];
        pigAmount.innerText = task['pigsty']['pigAmount'];
        lastFedTask.innerText = getFormattedDateTime(
            task['pigsty']['lastFeedTime']);
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
    document.getElementById("planed_time").innerText = getFormattedTime(
        curTask["scheduleEntry"]["timeStart"]);

    let actions = document.getElementById("actions")

    let sorted = curTask.subTasks.sort(
        (a, b) => Date.parse(a.scheduleEntry.timeStart) - Date.parse(
            b.scheduleEntry.timeStart))
    let doStop = false;

    for (let subTask of sorted) {
      let div = document.createElement("div");
      div.setAttribute("class", "form-check mb-2");

      let input = document.createElement("input");
      input.setAttribute("class", "form-check-input");
      input.setAttribute("type", "checkbox");
      input.setAttribute("class", "form-check-input");
      input.setAttribute("value", subTask["subTaskType"]);
      input.setAttribute("name", subTask["subTaskType"]);
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

      if (!subTask.complete) {
        doStop = true;
      }
      if (doStop) {
        break;
      }
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
      input.setAttribute("disabled", "disabled");

      console.log("completeSubTask(" + token + ", " + subTaskId + ") finish");
    },
  });
}

function showTaskComplete() {
  document.getElementById("bodyTitle").setAttribute(
      "class", "alert alert-success"
  );

  document.getElementById("btnCompleteTask").setAttribute("disabled",
      "disabled");
  document.getElementById("btnCompleteSubTask").setAttribute("disabled",
      "disabled");
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

  updatePage();
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

  updatePage();
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
