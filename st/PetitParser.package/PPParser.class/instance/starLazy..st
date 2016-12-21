operators-repeating
starLazy: aParser
	"Answer a new parser that parses the receiver zero or more times until it reaches aParser. This is a lazy non-blind implementation of the star operator. aParser is not consumed."
	
	^ PPLazyRepeatingParser on: self limit: aParser