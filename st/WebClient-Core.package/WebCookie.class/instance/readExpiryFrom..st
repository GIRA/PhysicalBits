initialize
readExpiryFrom: aString
	"Read a cookie expiry date from the given string.
	The expiration date is specified in the 'Wdy, DD-Mon-YYYY HH:MM:SS GMT' format."
	
	| tokens |
	^[
		tokens := aString withBlanksTrimmed.
		(tokens copyAfterLast: Character space) = 'GMT'
			ifFalse:[^DateAndTime new].
		tokens := (tokens copyAfter: Character space) copyUpToLast: Character space.
		(DateAndTime readFrom: tokens readStream) offset: Duration zero.
	] on: Error do:[:ex| ex return: DateAndTime new].