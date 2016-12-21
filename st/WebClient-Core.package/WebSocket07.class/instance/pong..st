control requests
pong: data
	"Send a pong control request"

	self send: data code: 10.