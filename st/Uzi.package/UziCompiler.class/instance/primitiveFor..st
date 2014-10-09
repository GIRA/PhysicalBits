private
primitiveFor: selector
	^ UziPrimitive named: (selector caseOf: {
		[#at:] -> [#getValue].
		[#at:put:] -> [#setValue]
	} otherwise: [UziInvalidPrimitiveCallError signal: selector])