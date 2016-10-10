accessing
labelsFrom: instructionOffset
	"Returns the labels dictionary with the offsets updated relative to the argument"
	^ (labels associations
		collect: [:each | each key -> (each value - instructionOffset)])
		as: Dictionary 