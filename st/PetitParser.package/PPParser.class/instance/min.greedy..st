operators-repeating
min: anInteger greedy: aParser
	"Answer a new parser that parses the receiver at least anInteger times until it reaches aParser. This is a greedy non-blind implementation. aParser is not consumed."
	
	^ (self starGreedy: aParser) setMin: anInteger