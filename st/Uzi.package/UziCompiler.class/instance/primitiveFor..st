private
primitiveFor: selector
	^ UziPrimitive named: (selector caseOf: {
		[#read] -> [#getValue].
		[#read:] -> [#getValue].
		[#write:] -> [#setValue].
		[#write:value:] -> [#setValue].
		[#toggle] -> [#toggle].
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