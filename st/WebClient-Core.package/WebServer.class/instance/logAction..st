accessing
logAction: aBlockOrMessage
	"The current log action (a block or message).
	Example:
		WebServer new logAction: [:aString| Transcript show: aString].
		WebServer new logAction: (MessageSend receiver: Transcript selector: #show).
	"

	logAction := aBlockOrMessage