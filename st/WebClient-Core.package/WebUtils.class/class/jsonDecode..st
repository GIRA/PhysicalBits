json-decode
jsonDecode: stream
	"Decodes an arbitrary JSON encoded value from the given stream"

	stream skipSeparators.
	^stream peek caseOf: {
		[$"]		->	[self jsonStringFrom: stream].
		[$t]		->	[self jsonTrueFrom: stream].
		[$f]		->	[self jsonFalseFrom: stream].
		[$n]		->	[self jsonNullFrom: stream].
		[${]		-> 	[self jsonMapFrom: stream].
		[$[]		->	[self jsonArrayFrom: stream].
	} otherwise:[self jsonNumberFrom: stream].
