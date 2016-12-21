operators-repeating
max: anInteger
	"Answer a new parser that parses the receiver at most anInteger times."
	
	^ self star setMax: anInteger