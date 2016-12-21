specifying
prefix: aParser do: aTwoArgumentBlock
	"Define a prefix operator aParser. Evaluate aTwoArgumentBlock with the operator and the term."

	self operator: #build:prefix: parser: aParser do: aTwoArgumentBlock