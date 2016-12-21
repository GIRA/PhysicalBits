authentication
passwordHashAt: key
	"Answer the password hash for the given key"

	^vault at: key ifAbsent:['']