websockets
webSocketTo: serverUrl
	"Attempt to connect to the given server url using the WebSocket api"

	"Testing:

		1) Launch WebSocket example.
		2) Run the following code:
		
		| ws |
		ws := WebClient webSocketTo: 'http://localhost:8080/broadcast'.
		ws onMessage:[:data| Transcript cr; show: data].
		ws fork.
		ws send: 'This is the first message'.
		ws send: 'This is the last message'.
		ws close.

	"

	^self webSocketTo: serverUrl protocol: nil