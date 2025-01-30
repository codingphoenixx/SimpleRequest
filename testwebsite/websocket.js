let socket;

function connect(){
    socket = new WebSocket('ws://127.0.0.1:49152/ws/testsocket');

    socket.onopen = function(event) {
        console.log('Connection established');
    };

    socket.onmessage = function(event) {
        console.log('Message from server:', event.data);
    };

    socket.onerror = function(error) {
        console.error('WebSocket Error:', error);
    };

    socket.onclose = function(event) {
        console.log('Connection closed');
    };
}

function sendMessage(){
    console.log('Send message to server');
    if(socket == null || socket.closed){
        console.error("Socket not connected.")
    }else {
        socket.send('Hello from client');
    }
}
