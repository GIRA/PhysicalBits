as yet unclassified
tearDown
	stats ifEmpty: [^ self].
	self class outputDirectory
		forceNewFileNamed: 'CompileStats.csv' do: [:file |
			self writeColumnsOn: file.
			self writeAggregateDataOn: file.
			self writeActualDataOn: file]