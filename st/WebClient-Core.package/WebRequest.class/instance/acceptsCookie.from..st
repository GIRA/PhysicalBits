testing
acceptsCookie: aCookie from: webClient
	"Returns true if this cookie is applicable to this request"

	aCookie domain ifNotNil:[:domain|
		(webClient serverName endsWith: domain) ifFalse:[^false].
	].
	aCookie path ifNotNil:[:path|
		(self url beginsWith: path) ifFalse:[^false].
	].
	aCookie secure ifNotNil:[:secure|
		secure ifTrue:[webClient scheme = 'https' ifFalse:[^false]].
	].
	aCookie expiry ifNotNil:[:expiry|
		aCookie isExpired ifTrue:[^false].
	].
	^true