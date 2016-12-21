operators-repeating
plus
	"Answer a new parser that parses the receiver one or more times."

	^ self star setMin: 1