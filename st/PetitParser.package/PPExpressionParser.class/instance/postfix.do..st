specifying
postfix: aParser do: aTwoArgumentBlock
	"Define a postfix operator aParser. Evaluate aTwoArgumentBlock with the term and the operator."

	self operator: #build:postfix: parser: aParser do: aTwoArgumentBlock