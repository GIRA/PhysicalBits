building messages
specialMessageSend: rcvr selector: selector arguments: args
	"
	If the selector is a special message then build the corresponding node
	and return it. Otherwise return nil.
	"
	(self conditional: rcvr selector: selector arguments: args) ifNotNil: [:result | ^ result].
	(self loop: rcvr selector: selector arguments: args) ifNotNil: [:result | ^ result].
	(self scripting: rcvr selector: selector arguments: args) ifNotNil: [:result | ^ result].
	^ nil