initialize
initialize
	"Initialize the receiver"

	super initialize.
	method := 'GET'.
	protocol := 'HTTP/1.1'.
	headers := OrderedCollection new.