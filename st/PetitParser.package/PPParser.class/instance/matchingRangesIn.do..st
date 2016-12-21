parsing
matchingRangesIn: anObject do: aBlock
	"Search anObject repeatedly for the matches of the receiver. Evaluate aBlock with the range of each match (index of first character to: index of last character)."

	self token
		matchesIn: anObject
		do: [ :token | aBlock value: (token start to: token stop) ]