private
primitiveFor: selector
	^ selector caseOf: {
		[#read] -> [#read].
		[#write:] -> [#write].
		[#toggle] -> [#toggle].
		[#servoDegrees:] -> [#servoDegrees].
		[#servoWrite:] -> [#servoWrite].
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
		[#=] -> [#equals].
		[#~=] -> [#notEquals].
		[#>] -> [#greaterThan].
		[#>=] -> [#greaterThanOrEquals].
		[#<] -> [#lessThan].
		[#<=] -> [#lessThanOrEquals].
		[#negated] -> [#negate].
	} otherwise: [UziInvalidPrimitiveCallError signal: selector]