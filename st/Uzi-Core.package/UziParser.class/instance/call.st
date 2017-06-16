grammar-calls
call
	^ ws , (identifier , argsList) token , ws
		map: [:ign1 :token :ign2 || selector arguments |
			selector := token parsedValue first.
			arguments := token parsedValue second.
			(UziPrimitiveCallNode
				selector: selector
				arguments: arguments
				primitive: nil)
				token: token]
		