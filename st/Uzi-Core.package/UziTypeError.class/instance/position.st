accessing
position
	"
	Returns an association whose key is the line on the src where the 
	error occurred and the value is the position inside that line.
	"
	| index position |
	position := node token start.
	index := 1.
	node token collection lineIndicesDo: [:start :ign :end |
		(position between: start and: end)
			ifTrue: [^ index -> (position - start + 1)]
			ifFalse: [index := index + 1]].
	"We didn't find the error. WTF?"
	^ nil