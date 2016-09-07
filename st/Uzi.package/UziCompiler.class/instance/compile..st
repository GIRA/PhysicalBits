accessing
compile: aString 
	| nodes |
	nodes := UziParser parse: aString.
	nodes isPetitFailure ifTrue: [^ UziParseError signal: nodes asString].
	^ Uzi program: [:p |
		program := p.
		self visitCollection: nodes]