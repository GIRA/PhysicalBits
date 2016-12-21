operators-mapping
>=> aBlock
	"Answer a new parser that wraps the receiving parser with a two argument block. The first argument is the parsed stream, the second argument a continuation block on the delegate parser."

	^ PPWrappingParser on: self block: aBlock