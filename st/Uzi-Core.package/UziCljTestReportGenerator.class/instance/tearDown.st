as yet unclassified
tearDown
	self class outputDirectory
		forceNewFileNamed: 'checker_tests_autogen.clj' do: [:file | 
			tests keys asSortedCollection do: [:key |
				self write: (tests at: key) named: key on: file]].