sessions
sessionAt: key
	"Answer the session associated with the given key"

	^mutex critical:[sessions at: key ifAbsent:[nil]].