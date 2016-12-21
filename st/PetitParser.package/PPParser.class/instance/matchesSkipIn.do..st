parsing
matchesSkipIn: anObject do: aBlock
	"Search anObject repeatedly for the matches of the receiver. Evaluate aBlock for each match with the matched parse-tree as the argument. Skip over matches."

	(self ==> aBlock / #any asParser) star parse: anObject