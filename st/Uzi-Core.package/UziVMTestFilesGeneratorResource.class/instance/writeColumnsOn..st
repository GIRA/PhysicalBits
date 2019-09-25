as yet unclassified
writeColumnsOn: file
	| columns |
	columns := stats anyOne collect: [:each | each key].
	columns do: [:c | file nextPutAll: c asString]
		separatedBy: [file nextPutAll: ','].
	file crlf