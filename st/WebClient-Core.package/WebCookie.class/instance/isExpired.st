testing
isExpired
	"Return true if this cookie is expired"
	
	expiry ifNil:[^false].
	^expiry asUTC < DateAndTime now asUTC