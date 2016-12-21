sessions
sessionAt: key ifAbsentPut: session
	"Answer the session associated with the given key"

	^mutex critical:[sessions at: key ifAbsentPut:[session]].