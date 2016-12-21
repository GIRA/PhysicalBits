cookies
cookieAt: aString ifAbsent: aBlock
	"Answer the value for a cookie with the given name"

	self cookiesDo:[:name :value| name = aString ifTrue:[^value]].
	^aBlock value