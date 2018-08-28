grammar-scriptRefs
call
	^ ws , (scriptReference, argsList) token , ws
		map: [:ign1 :token :ign2 || selector arguments |
			selector := token parsedValue first.
			arguments := token parsedValue second.
			(UziCallNode 
				selector: selector
				arguments: arguments)
				token: token]
		