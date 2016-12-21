operators-repeating
plusGreedy: aParser
	"Answer a new parser that parses the receiver one or more times until it reaches aParser. This is a greedy non-blind implementation of the star operator. aParser is not consumed."
	
	^ (self starGreedy: aParser) setMin: 1