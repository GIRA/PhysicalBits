grammar-statements
assignment 
	^ ws, (variable , ws , $= asParser , ws , expression , ws , $; asParser) token, ws
		map: [:ign1 :token :ign2 || nodes |
			nodes := token parsedValue.
			(UziAssignmentNode left: nodes first right: nodes fifth)
				token: token]