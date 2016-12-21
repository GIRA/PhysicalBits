operators-repeating
max: anInteger greedy: aParser
	"Answer a new parser that parses the receiver at most anInteger times until it reaches aParser. This is a greedy non-blind implementation. aParser is not consumed."
	
	^ (self starGreedy: aParser) setMax: anInteger