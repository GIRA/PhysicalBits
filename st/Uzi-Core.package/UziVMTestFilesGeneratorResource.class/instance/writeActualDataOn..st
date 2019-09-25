as yet unclassified
writeActualDataOn: file
	stats do: [:data |
		data do: [:kvp || datum |
				datum := kvp value.
				file nextPutAll: (datum ifNil: [''] ifNotNil: [:d | d asString])]
			separatedBy: [file nextPutAll: ','].
		file crlf]