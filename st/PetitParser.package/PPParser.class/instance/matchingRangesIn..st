parsing
matchingRangesIn: anObject
	"Search anObject repeatedly for the matches of the receiver. Answer an OrderedCollection of ranges of each match (index of first character to: index of last character)."
	
	| result |
	result := OrderedCollection new.
	self
		matchingRangesIn: anObject
		do: [ :value | result addLast: value ].
	^ result