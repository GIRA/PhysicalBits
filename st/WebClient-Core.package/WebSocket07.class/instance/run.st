running
run
	"Run the WebSocket's input process."

	[[[stream isConnected] whileTrue:[
		self readFrame.
	]] on: Error do:[:ex| self handleError: ex]] ensure:[
		[stream socket destroy] on: Error do:[:ex|].
		self handleClose.
	].
