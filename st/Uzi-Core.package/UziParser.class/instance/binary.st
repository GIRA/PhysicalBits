grammar-binary
binary
	^ ws , (expressionNotBinary , (ws , binarySelector , expressionNotBinary) token plus) token, ws
	map: [:ign1 :token :ign2 | self buildBinaryExpression: token]