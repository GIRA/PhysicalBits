grammar-calls
call
	^ ws , (identifier, argsList) token , ws
		map: [:ign1 :token :ign2 || selector arguments |
			selector := token parsedValue first.
			arguments := token parsedValue second.
			((self primitiveForSelector: selector)
				ifNil: [UziScriptCallNode script: selector]
				ifNotNil: [:primitive |
					UziPrimitiveCallNode
						selector: selector
						arguments: arguments
						primitive: primitive])
				token: token]
		