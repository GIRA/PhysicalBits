accessing
compile: aString 
	| nodes scripts |
	nodes := UziParser parse: aString.
	nodes isPetitFailure ifTrue: [^ UziParseError signal: nodes asString].	
	variables := OrderedCollection new.
	scripts := nodes collect: [:node | self visit: node].
	^ UziProgram variables: variables scripts: scripts