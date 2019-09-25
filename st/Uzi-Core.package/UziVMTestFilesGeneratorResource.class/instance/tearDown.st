as yet unclassified
tearDown
	stats ifEmpty: [^ self].
	self class outputDirectory
		forceNewFileNamed: 'CompileStats.csv' do: [:file || columns |
			columns := stats anyOne collect: [:each | each key].
			columns do: [:c | file nextPutAll: c asString]
				separatedBy: [file nextPutAll: ','].
			file crlf.
			stats do: [:data |
				data do: [:kvp || datum |
						datum := kvp value.
						file nextPutAll: (datum ifNil: [''] ifNotNil: [:d | d asString])]
					separatedBy: [file nextPutAll: ','].
				file crlf]]