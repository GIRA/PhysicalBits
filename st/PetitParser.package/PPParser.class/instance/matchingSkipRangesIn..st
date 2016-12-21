parsing
matchingSkipRangesIn: anObject
	"Search anObject repeatedly for the matches of the receiver. Skip over matches. Answer an OrderedCollection of ranges of each match (index of first character to: index of last character)."
	
	| result |
	result := OrderedCollection new.
	self
		matchingSkipRangesIn: anObject
		do: [ :value | result addLast: value ].
	^ result