sessions
sessionAt: key put: session
	"Store the session associated with the given key"

	^mutex critical:[sessions at: key put: session].