operators-repeating
min: anInteger
	"Answer a new parser that parses the receiver at least anInteger times."
	
	^ self star setMin: anInteger