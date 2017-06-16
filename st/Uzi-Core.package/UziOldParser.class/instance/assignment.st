grammar-expressions
assignment 
	^ (variable , ws , ':=' asParser , ws , expression) token
		map: [:token || nodes |
			nodes := token parsedValue.
			(UziAssignmentNode left: nodes first right: nodes last)
				token: token]