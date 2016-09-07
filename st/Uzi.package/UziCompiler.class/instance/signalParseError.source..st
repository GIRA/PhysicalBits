private
signalParseError: parseFailure source: src
	| msg |
	msg := '{1} at {2}' format: {
		parseFailure message.
		(self positionOf: parseFailure in: src)
			ifNil: [parseFailure position]
			ifNotNil: [:pos | 'line {1}:{2}' format: { pos key. pos value}]}.
	^ UziParseError signal: msg