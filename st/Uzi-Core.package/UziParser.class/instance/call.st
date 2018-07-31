grammar-scriptRefs
call
	^ ws , (scriptReference, argsList) token , ws
		map: [:ign1 :token :ign2 || ref arguments |
			ref := token parsedValue first.
			arguments := token parsedValue second.
			((self primitiveForSelector: ref scriptName)
				ifNil: [UziScriptCallNode 
					script: ref
					arguments: arguments]
				ifNotNil: [:primName |
					UziPrimitiveCallNode
						selector: ref scriptName
						arguments: (arguments collect: [:kvp | kvp value])
						primitiveName: primName])
				token: token]
		