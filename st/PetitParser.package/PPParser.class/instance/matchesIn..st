parsing
matchesIn: anObject
	"Search anObject repeatedly for the matches of the receiver. Answered an OrderedCollection of the matched parse-trees."

	| result |
	result := OrderedCollection new.
	self 
		matchesIn: anObject
		do: [ :each | result addLast: each ].
	^ result