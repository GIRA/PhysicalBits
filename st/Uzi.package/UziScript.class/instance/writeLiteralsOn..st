as yet unclassified
writeLiteralsOn: aStream
	| count size |
	aStream nextPut: literals size.
	size := 0.
	count := literals size.
	aStream nextPut: ((count bitShift: 2) bitOr: size).
	literals do: [:lit | aStream nextPut: lit value]