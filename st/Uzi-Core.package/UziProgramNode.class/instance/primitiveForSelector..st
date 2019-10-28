accessing
primitiveForSelector: selector
	| result |
	result := primitivesDict at: selector
		ifPresent: [:prim | prim name]
		ifAbsent: [nil].
	
	"HACK: As soon as I implement the implicit import of core.uzi, I should remove the following code"
	self flag: #Richo. 
	result ifNotNil: [^ result].
	
	^ 	(selector subStrings: '.') last caseOf: {
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
		[#sqrt] -> [#sqrt].
		[#==] -> [#equals].
		[#!=] -> [#notEquals].
		[#>] -> [#greaterThan].
		[#>=] -> [#greaterThanOrEquals].
		[#<] -> [#lessThan].
		[#<=] -> [#lessThanOrEquals].
		[#!] -> [#negate].
		[#delayMs] -> [#delayMs].
		[#&] -> [#bitwiseAnd].
		[#|] -> [#bitwiseOr].
		[#millis] -> [#millis].
		[#coroutine] -> [#coroutine].
		[#serialWrite] -> [#serialWrite].
		[#round] -> [#round]. 
		[#ceil] -> [#ceil]. 
		[#floor] -> [#floor]. 
		[#sqrt] -> [#sqrt]. 
		[#abs] -> [#abs]. 
		[#ln] -> [#ln]. 
		[#log10] -> [#log10]. 
		[#exp] -> [#exp]. 
		[#pow10] -> [#pow10]. 
		[#asin] -> [#asin]. 
		[#acos] -> [#acos]. 
		[#atan] -> [#atan].
		[#**] -> [#power]. 
		[#isOn] -> [#isOn]. 
		[#isOff] -> [#isOff]. 
		[#%] -> [#mod]. 
		[#constrain] -> [#constrain]. 
		[#randomInt] -> [#randomInt]. 
		[#random] -> [#random]. 
		[#isEven] -> [#isEven]. 
		[#isOdd] -> [#isOdd]. 
		[#isPrime] -> [#isPrime]. 
		[#isWhole] -> [#isWhole]. 
		[#isPositive] -> [#isPositive]. 
		[#isNegative] -> [#isNegative]. 
		[#isDivisibleBy] -> [#isDivisibleBy].
		[#seconds] -> [#seconds].
		[#isCloseTo] -> [#isCloseTo].
		[#delayS] -> [#delayS].
		[#delayM] -> [#delayM].
		[#minutes] -> [#minutes].
		[#sonarDistCm] -> [#sonarDistCm]
	} otherwise: nil