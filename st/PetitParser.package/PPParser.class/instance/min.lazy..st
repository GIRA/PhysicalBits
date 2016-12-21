operators-repeating
min: anInteger lazy: aParser
	"Answer a new parser that parses the receiver at least anInteger times until it reaches aParser. This is a lazy non-blind implementation. aParser is not consumed."
	
	^ (self starLazy: aParser) setMin: anInteger