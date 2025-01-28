
const socket = new WebSocket('ws://127.0.0.1:8080/testsocket');

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