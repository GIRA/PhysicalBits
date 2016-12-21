private
handleMessage: msgData type: msgType
	"Handle an incoming message of the given type"

	self handle: 'message' arguments: (Array with: msgData with: msgType with: self)