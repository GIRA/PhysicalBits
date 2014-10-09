encoding scripts
writeLiterals: anUziScript
	| grouped |
	"First we sort and store them so that later we can get their indexes"
	literals := anUziScript literals asSortedCollection: [:a :b | a value < b value].
	"We start the section by writing how many items to expect"
	stream nextPut: literals size.
	"We group by sizes (in bytes)"
	grouped := literals groupBy: [:literal | 
		(1 to: 4) detect: [:e | literal value < (2 raisedTo: 8 * e)]]
		having: [:ign | true].
	"And then we write each group"
	grouped keysAndValuesDo: [:size :group |
		"The first byte says how many and of what size"
		stream nextPut: ((group size bitShift: 2) bitOr: size - 1).
		"Then we can write each value"
		group do: [:lit | stream nextPut: lit value]]