control requests
handleClose: payload
	"Close is being sent by remote in process of shutting down connection"

	closing ifFalse:[^self close].

	"We're done. We've received the close and responded to it"
	stream close.

