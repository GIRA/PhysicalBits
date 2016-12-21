initialize
username: username password: password
	"Use the given username/password as credentials"

	| params retry |
	params := self isProxyAuth ifTrue:[client proxyParams] ifFalse:[client authParams].
	retry := false.
	(params at: #username ifAbsent:[nil]) = username ifFalse:[
		params at: #username put: username.
		retry := true.
	].
	(params at: #password ifAbsent:[nil]) = password ifFalse:[
		params at: #password put: password.
		retry := true.
	].
	retry ifTrue:[self resume: true].
