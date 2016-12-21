operators-repeating
starGreedy: aParser
	"Answer a new parser that parses the receiver zero or more times until it reaches aParser. This is a greedy non-blind implementation of the star operator. aParser is not consumed."
	
	^ PPGreedyRepeatingParser on: self limit: aParser