operators
, aParser 
	"Answer a new parser that parses the receiver followed by aParser."

	^ PPSequenceParser with: self with: aParser