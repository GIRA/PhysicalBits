as yet unclassified
writeBytecodesOn: aStream
	bytecodes do: [:each |
		aStream nextPut: ((each code bitShift: 4) 
					bitOr: (literals indexOf: each argument) - 1)]