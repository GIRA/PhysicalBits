encoding programs
writeVariables: program
	| grouped |
	"First we sort and store them so that later we can get their indexes.
	We separate integers and floats because floats will always be encoded last."
	variables := program variables 
		asSortedCollection: [:a :b | a size < b size or: [a value < b value]].
	"We start the section by writing how many items to expect"
	stream nextPut: variables size.
	"We group variables by size (in bytes)"
	grouped := variables groupBy: #size having: [:ign | true].
	"And then we write each group"
	grouped keysAndValuesDo: [:size :group |
		"The first byte says how many and of what size"
		stream nextPut: ((group size bitShift: 2) bitOr: size - 1).
		"Then we can write each value"
		group do: [:lit | self writeVariable: lit ofSize: size]]