operators-repeating
times: anInteger
	"Answer a new parser that parses the receiver exactly anInteger times."
	
	^ self min: anInteger max: anInteger