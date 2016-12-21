private
buildOn: aParser
	^ self buildSelectors inject: aParser into: [ :term :selector |
		| list |
		list := operators at: selector ifAbsent: [ #() ].
		list isEmpty
			ifTrue: [ term ]
			ifFalse: [
				self
					perform: selector with: term 
					with: (list size = 1
						ifTrue: [ list first first ==> [ :operator | Array with: list first second with: operator ] ]
						ifFalse: [ 
							list
								inject: PPChoiceParser new
								into: [ :choice :each | choice / (each first ==> [ :operator | Array with: each second with: operator ]) ] ]) ] ]