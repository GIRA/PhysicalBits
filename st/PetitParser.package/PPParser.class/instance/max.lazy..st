operators-repeating
max: anInteger lazy: aParser
	"Answer a new parser that parses the receiver at most anInteger times until it reaches aParser. This is a lazy non-blind implementation. aParser is not consumed."
	
	^ (self starLazy: aParser) setMax: anInteger