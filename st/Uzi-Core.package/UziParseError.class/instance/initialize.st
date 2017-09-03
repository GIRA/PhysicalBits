initialization
initialize
	| msg |
	super initialize.
	msg := '{1} at {2}' format: {
		failure message.
		(self positionOf: failure in: source)
			ifNil: [failure position]
			ifNotNil: [:pos | 'line {1}:{2}' format: { pos key. pos value}]}.
	self messageText: msg
	