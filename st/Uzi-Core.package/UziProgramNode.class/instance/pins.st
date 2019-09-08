accessing
pins
	| set |
	set := Set new.
	^ Array streamContents: [:stream |
		self select: [:node | node isPinLiteral]
			thenDo: [:pin || name |
				name := pin name.
				(set includes: name)
					ifFalse: [set add: name.
						stream nextPut: pin]]].