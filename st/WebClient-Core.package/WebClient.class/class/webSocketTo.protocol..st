websockets
webSocketTo: serverUrl protocol: protocol
	"Attempt to connect to the given server url using the WebSocket api"

	^self webSocket00: serverUrl protocol: protocol