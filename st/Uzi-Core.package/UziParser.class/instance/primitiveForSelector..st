private
primitiveForSelector: selector
	^ selector caseOf: {
		[#turnOn] -> [#turnOn].
		[#turnOff] -> [#turnOff].
		[#read] -> [#read].
		[#write] -> [#write].
		[#toggle] -> [#toggle].
		[#servoDegrees] -> [#servoDegrees].
		[#servoWrite] -> [#servoWrite].
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
		[#==] -> [#equals].
		[#!=] -> [#notEquals].
		[#>] -> [#greaterThan].
		[#>=] -> [#greaterThanOrEquals].
		[#<] -> [#lessThan].
		[#<=] -> [#lessThanOrEquals].
		[#not] -> [#negate].
		[#delayMs] -> [#delayMs].
		[#&] -> [#bitwiseAnd].
		[#|] -> [#bitwiseOr].
		[#millis] -> [#millis].
		[#coroutine] -> [#coroutine].
		[#serialWrite] -> [#serialWrite].
	} otherwise: nil