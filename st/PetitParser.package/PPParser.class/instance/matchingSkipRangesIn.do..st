parsing
matchingSkipRangesIn: anObject do: aBlock
	"Search anObject repeatedly for the matches of the receiver. Skip over matches. Evaluate aBlock with the range of each match (index of first character to: index of last character)."
	
	self token
		matchesSkipIn: anObject
		do: [ :token | aBlock value: (token start to: token stop) ]