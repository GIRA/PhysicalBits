operators-repeating
star
	"Answer a new parser that parses the receiver zero or more times. This is a greedy and blind implementation that tries to consume as much input as possible and it does not consider what comes afterwards."

	^ PPPossessiveRepeatingParser on: self