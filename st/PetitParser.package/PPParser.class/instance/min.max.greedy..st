operators-repeating
min: aMinInteger max: aMaxInteger greedy: aParser
	"Answer a new parser that parses the receiver at least aMinInteger and at most aMaxInteger times until it reaches aParser. This is a greedy non-blind implementation. aParser is not consumed."
	
	^ (self starGreedy: aParser) setMin: aMinInteger; setMax: aMaxInteger