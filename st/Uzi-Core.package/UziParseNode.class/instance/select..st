enumerating
select: predicateBlock
	^ Array streamContents: [:stream |
		self select: predicateBlock thenDo: [:node | stream nextPut: node]]