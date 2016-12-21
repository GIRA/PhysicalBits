operators-repeating
plusLazy: aParser
	"Answer a new parser that parses the receiver one or more times until it reaches aParser. This is a lazy non-blind implementation of the star operator. aParser is not consumed."
	
	^ (self starLazy: aParser) setMin: 1