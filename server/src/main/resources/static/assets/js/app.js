var stompClient = null;
$(function () {
    setConnected(false);
    $("#connect").click(function() {
        connectToWebsocket();
    });

    $("#disconnect").click(function() {
        disconnect();
    });
});

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#client-container").show();

        updateButtonParams('#connect', 'Connected!', 'btn-success', 'btn-outline-success');
        updateButtonParams('#disconnect', 'Disconnect', 'btn-outline-danger', 'btn-danger');
    }
    else {
        $("#client-container").hide();

        updateButtonParams('#connect', 'Connect', 'btn-outline-success', 'btn-success');
        updateButtonParams('#disconnect', 'Disconnected!', 'btn-danger', 'btn-outline-danger');
    }
    $("#messages").html("");
}

function updateButtonParams(elementId, buttonText, classToRemove, classToAdd) {
    $(elementId).html(buttonText);
    $(elementId).removeClass(classToRemove);
    $(elementId).addClass(classToAdd);
}

function connectToWebsocket() {
    var endpoint = 'http://localhost:8080/server';
    var socket = new SockJS(endpoint);
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/client/manager-ui', function (data) {
            buildClientList(data.body);
        });
    });
}

function buildClientList(_clients) {
    var clients = JSON.parse(_clients);
    $("#clients").empty();
    for(var i = 0; i < clients.length; i++) {
        var client = clients[i];
        var clientCard = createClientCard(i+1, client.clientId, client.active, client.connectionSessionId);
        $("#clients").append(clientCard);
    }
}

function createClientCard(index, clientId, status, sessionId) {
    if(clientId === 'manager-ui') {
        return '<div class="col-12 col-sm-6 col-md-4 p-3">'+
            '<div class="h-100 p-4 shadow rounded-3 bg-warning bg-opacity-50 bg-gradient">'+
                clientId +
                '<p class="text-muted">This application</p>'+
            '</div>'+
        '</div>';
    }

    return '<div class="col-12 col-sm-6 col-md-4 p-3">'+
        '<div class="h-100 bg-white p-4 shadow rounded-3">'+
            '<h5 class="card-title text-bold">Client ' + index + '</h5>'+
            '<p class="card-subtitle mb-2 text-muted">' + clientId + '</p>'+
            '<p class="card-text mt-4"><strong>Status: </strong>'+ status +'</p>'+
            '<div class="input-group">'+
               '<input id="client'+index+'" type="text" class="form-control form-control-sm" value="INVERT" >'+
               '<button class="btn btn-sm btn-outline-primary dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">Send</button>'+
               '<ul class="dropdown-menu dropdown-menu-end">'+
                 '<li><a class="dropdown-item" href="#" onClick=\'sendCommand(\"'+clientId+'\", \"#client' + index + '\")\'>Direct to Client</a></li>'+
                 '<li><a class="dropdown-item" href="#" onClick=\'sendCommandThroughServer(\"'+clientId+'\", \"#client' + index + '\")\'>Through Server</a></li>'+
               '</ul>'+
             '</div>'+
        '</div>'+
    '</div>';
}

function sendCommand(clientId, inputId) {
    var command = $(inputId).val();
    stompClient.send('/client/'+clientId, {}, command);
}

function sendCommandThroughServer(clientId, inputId) {
    var command = $(inputId).val();
    stompClient.send('/app/'+clientId, {}, command);
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    buildClientList('[]');
}