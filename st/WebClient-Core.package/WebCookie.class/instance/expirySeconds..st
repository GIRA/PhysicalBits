accessing
expirySeconds: maxAge
	"Compute the cookie's expiry age from the given number of seconds"

	maxAge = 0 ifTrue:[self expiry: DateAndTime new].
	self expiry: DateAndTime now + maxAge seconds.