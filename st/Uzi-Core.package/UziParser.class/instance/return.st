grammar-expressions
return
	^ ($^ asParser, ws, expression) token
		map: [:token || value |
			value := token parsedValue last.
			(UziReturnNode value: value)
				token: token withoutTrailingBlanks]