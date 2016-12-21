specifying
left: aParser do: aThreeArgumentBlock
	"Define an operator aParser that is left-associative. Evaluate aThreeArgumentBlock with the first term, the operator, and the second term."
	
	self operator: #build:left: parser: aParser do: aThreeArgumentBlock