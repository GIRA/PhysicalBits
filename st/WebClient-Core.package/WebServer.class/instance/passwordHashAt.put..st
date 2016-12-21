authentication
passwordHashAt: key put: hash
	"Store the password hash for the key."

	^vault at: key put: hash