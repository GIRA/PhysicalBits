initializing
initialize
	super initialize.
	node token
		ifNil: [self messageText: description]
		ifNotNil: [:token | self 
			messageText: ('{1} at line {2}:{3}' 
			format: { description . token line . token column })]