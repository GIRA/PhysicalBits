private
primitiveFor: selector
	^ UziPrimitive named: (selector caseOf: {
		[#at:] -> [#getValue].
		[#at:put:] -> [#setValue].
		[#toggle:] -> [#toggle].
		[#+] -> [#add].
		[#-] -> [#subtract].
		[#*] -> [#multiply].
		[#/] -> [#divide].
		[#sin] -> [#sin].
		[#cos] -> [#cos].
		[#tan] -> [#tan].
		[#arcSin] -> [#arcSin].
		[#arcCos] -> [#arcCos].
		[#arcTan] -> [#arcTan].
		[#squared] -> [#squared].
		[#sqrt] -> [#sqrt].
	} otherwise: [UziInvalidPrimitiveCallError signal: selector])