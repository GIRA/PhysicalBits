private
primitiveNameFor: selector
	^ selector caseOf: {
		[#at:] -> [#getValue].
		[#at:put:] -> [#setValue]
	} 