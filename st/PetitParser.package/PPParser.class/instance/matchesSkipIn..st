parsing
matchesSkipIn: anObject
	"Search anObject repeatedly for the matches of the receiver. Answer an OrderedCollection of the matched parse-trees. Skip over matches."

	| result |
	result := OrderedCollection new.
	self 
		matchesSkipIn: anObject
		do: [ :each | result addLast: each ].
	^ result