encoding programs
writeVariables: program
	| grouped floats integers |
	"First we sort and store them so that later we can get their indexes.
	We separate integers and floats because floats will always be encoded last."
	integers := (program variables select: #isInteger) asSortedCollection: [:a :b | a value < b value].
	floats := (program variables select: #isFloat) asSortedCollection: [:a :b | a value < b value].
	variables := integers asArray, floats asArray.
	"We start the section by writing how many items to expect"
	stream nextPut: variables size.
	"We group integers by sizes (in bytes)"
	grouped := integers groupBy: [:literal | 
		(1 to: 4) detect: [:e | literal value < (2 raisedTo: 8 * e)]]
		having: [:ign | true].
	"If we have floats we add them all on the group 4"
	floats ifNotEmpty: [(grouped 
		at: 4 
		ifAbsentPut: OrderedCollection new) 
			addAll: floats].
	"And then we write each group"
	grouped keysAndValuesDo: [:size :group |
		"The first byte says how many and of what size"
		stream nextPut: ((group size bitShift: 2) bitOr: size - 1).
		"Then we can write each value"
		group do: [:lit | self writeVariable: lit ofSize: size]]