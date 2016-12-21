auth
handleAuth: aBlock
	"Utility method to handle web authentication requests interactively.

	Use like here:
		| wc |
		wc := WebClient new.
		WebUtils handleAuth:[wc httpGet: 'http://some.auth.required'].
	"

	| user pass |
	^[aBlock value] on: WebAuthRequired do:[:ex|
		"Allow outer handlers to take over authentication"
		ex isNested ifFalse:[
			user := UIManager default request:  ex message, 
				'\Please enter your user name:' withCRs initialAnswer:''.
			user ifNotEmpty:[
				"Just a little obfuscation to avoid completely plain passwords"
				pass := (UIManager default requestPassword:
					'Please enter the password for "', user,'":') base64Encoded.
				 ex username: user password: [pass base64Decoded]]].
		ex pass].