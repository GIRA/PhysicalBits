accessing
compile: aString 
	| nodes scripts |
	nodes := UziParser parse: aString.
	nodes isPetitFailure ifTrue: [^ UziParseError signal: nodes asString].
	scripts := nodes collect: [:node | self visit: node].
	^ UziProgram scripts: scripts