initialization
initialize
	| msg |
	super initialize.
	msg := '{1} at {2}{3}' format: {
		failure message.
		file ifNil: ['line '] 
			ifNotNil: [file , ':'].
		(self positionOf: failure in: source)
			ifNil: [failure position]
			ifNotNil: [:pos | '{1}:{2}' format: { pos key. pos value}]}.
	self messageText: msg
	