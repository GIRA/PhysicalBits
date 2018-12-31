accessing
locals
	^ Array streamContents: [:stream |
		self currentScript ifNotNil: [:script |
			script variables withIndexDo: [:var :i |
				(stack at: fp + i ifAbsent: [nil])
					ifNotNil: [:value | stream nextPut: var name -> value]]]]