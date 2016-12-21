operators-mapping
answer: anObject
	"Answer a new parser that always returns anObject from a successful parse."

	^ self ==> [ :nodes | anObject ]