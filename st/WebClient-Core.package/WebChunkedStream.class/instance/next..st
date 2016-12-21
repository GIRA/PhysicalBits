accessing
next: anInteger 
	"Answer the next anInteger elements of my collection.  overriden for simplicity"

	[(position + anInteger >= readLimit) and:[chunkSize ~= 0]] 
		whileTrue:[self nextChunk].

	^super next: anInteger
