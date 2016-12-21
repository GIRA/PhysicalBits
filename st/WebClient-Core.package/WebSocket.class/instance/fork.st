running
fork
	"Fork the WebSocket's input process."

	process := [self run] newProcess.
	process resume.
