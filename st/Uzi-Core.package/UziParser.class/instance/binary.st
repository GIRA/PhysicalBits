grammar-binary
binary
	^ ws , (expressionNotBinary , (ws , binarySelector , expressionNotBinary) token plus) token, ws
	map: [:ign1 :token :ign2 || left |
		left := token parsedValue first.
		token parsedValue second 
			do: [:token2 || selector right |
				selector := token2 parsedValue second.
				right := token2 parsedValue third.
				left := (UziPrimitiveCallNode
					selector: token2 parsedValue second
					arguments: { left . right }
					primitive: nil)
					token: (PPToken 
						on: token collection
						start: token start
						stop: token2 stop
						value: token2 parsedValue)].
		left]