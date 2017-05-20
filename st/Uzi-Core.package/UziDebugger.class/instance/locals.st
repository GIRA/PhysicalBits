accessing
locals
	^ Array streamContents: [:stream |
		self currentScript ifNotNil: [:script |
			script variables withIndexDo: [:var :index |
				(stack at: fp + index ifAbsent: [nil])
					ifNotNil: [:value | stream nextPut: var name -> value]]]]