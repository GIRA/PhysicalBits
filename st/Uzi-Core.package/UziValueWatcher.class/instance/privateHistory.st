accessing
privateHistory
	history ifNotEmpty: [:list || begin |
		begin := list last key - 10000.
		list removeAllSuchThat: [:kvp | kvp key < begin]].
	^ history