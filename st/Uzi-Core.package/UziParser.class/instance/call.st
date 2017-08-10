grammar-calls
call
	^ ws , (identifier, argsList) token , ws
		map: [:ign1 :token :ign2 || selector arguments |
			selector := token parsedValue first.
			arguments := token parsedValue second.
			((self primitiveForSelector: selector)
				ifNil: [UziScriptCallNode 
					script: selector
					arguments: arguments]
				ifNotNil: [:primitive |
					UziPrimitiveCallNode
						selector: selector
						arguments: (arguments collect: [:kvp | kvp value])
						primitive: primitive])
				token: token]
		