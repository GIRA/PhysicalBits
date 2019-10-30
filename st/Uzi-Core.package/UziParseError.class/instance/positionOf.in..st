initialization
positionOf: parseFailure in: src 
	"
	Returns an association whose key is the line on the src where the 
	error occurred and the value is the position inside that line.
	"
	| index position |
	position := parseFailure position.
	position isString ifTrue: [^ nil].
	index := 1.
	position := position + 1.
	src lineIndicesDo: [:start :ign :end |
		(position between: start and: end)
			ifTrue: [^ index -> (position - start + 1)]
			ifFalse: [index := index + 1]].
	"We didn't find the error. WTF?"
	^ nil