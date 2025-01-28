
const socket = new WebSocket('ws://127.0.0.1:49152/testsocket');

socket.onopen = function(event) {
    console.log('Connection established');
    sendHelloMessage();
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

function sendHelloMessage(){
    console.log('Send message to server');
    socket.send('Hello Server');
}
