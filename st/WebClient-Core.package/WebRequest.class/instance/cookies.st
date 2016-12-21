cookies
cookies
	"Answer all the cookies defined in the request"

	| cookies |
	cookies := Dictionary new.
	self cookiesDo:[:name :value| cookies at: name put: value].
	^cookies