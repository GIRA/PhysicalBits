initialize
readFrom: aStream
	"Read a cookie from the given stream"

	| keyval key val |
	aStream skipSeparators.
	name := (aStream upTo: $=) withBlanksTrimmed.
	value := (aStream upTo: $;) withBlanksTrimmed.
	[aStream atEnd] whileFalse:[
		keyval := aStream upTo: $;.
		key := (keyval copyUpTo: $=) withBlanksTrimmed.
		val := (keyval copyAfter: $=) withBlanksTrimmed.
		key asLowercase caseOf: {
			['expires'] 	-> [self expiry: (self readExpiryFrom: val)].
			['path']		-> [self path: (WebUtils unquote: val)].
			['domain'] 	-> [self domain: (WebUtils unquote: val)].
			['secure'] 	-> [self secure: true].
			['version'] 	-> [self version: val].
			['httponly'] 	-> [self httpOnly: true].
			['comment']	 ->[self comment: val].
			['max-age']	->[self expirySeconds: val asNumber].
		} otherwise:[Transcript show: 'Unknown cookie field: ', key].
	].