listening
listenOn: aPort interface: anInterface backlogSize: backlog
	"Starts the server listening on the given port / network interface"
	
	| ipString |
	interface := anInterface.
	port := aPort.
	ipString := interface ifNil:['*'] ifNotNil:[
		String streamContents:[:s| interface do:[:b| s print: b] separatedBy:[s nextPut:$.]]].
	self log: 'listener starting (interface: ', ipString, ' port: ', port asString, ')'.
	listenerSocket := Socket newTCP.
	interface 
		ifNil:[listenerSocket listenOn: port backlogSize: backlog]
		ifNotNil:[listenerSocket listenOn: port backlogSize: backlog interface: interface].
	listenerSocket isWaitingForConnection ifFalse:[
		self error: 'Failed to listen(interface: ', interface printString, ' port: ', aPort printString,' )'.
	].
	self startListener.