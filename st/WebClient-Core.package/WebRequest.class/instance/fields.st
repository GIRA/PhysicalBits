fields
fields
	"Answer the fields for the given request."

	| fields |
	fields := Dictionary new.
	self getFields associationsDo:[:a| fields add: a].
	self postFields associationsDo:[:a| fields add: a].
	self multipartFields associationsDo:[:a| fields add: a].
	^fields
