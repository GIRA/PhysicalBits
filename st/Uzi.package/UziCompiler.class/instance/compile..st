accessing
compile: aString 
	| nodes |
	nodes := UziParser parse: aString.
	nodes isPetitFailure ifTrue: [^ self signalParseError: nodes source: aString].
	^ Uzi program: [:p |
		program := p.
		self visitCollection: nodes]