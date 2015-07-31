private
primitiveFor: selector
	^ UziPrimitive named: (selector caseOf: {
		[#at:] -> [#getValue].
		[#at:put:] -> [#setValue].
		[#toggle:] -> [#toggle]
	} otherwise: [UziInvalidPrimitiveCallError signal: selector])