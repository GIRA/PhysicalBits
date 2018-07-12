grammar-binary
buildBinaryExpression: token
	| left |
	left := token parsedValue first.
	token parsedValue second 
		do: [:token2 || selector right |
			selector := token2 parsedValue second.
			right := token2 parsedValue third.
			left := self buildBinaryExpression: selector left: left right: right.
			left token: (PPToken 
					on: token collection
					start: token start
					stop: token2 stop
					value: token2 parsedValue)].
	^ left