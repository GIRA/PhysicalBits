accessing
default
	"A default WebServer instance. Mostly for the examples."

	^Default ifNil:[Default := self new].