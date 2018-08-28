enumerating
select: predicateBlock thenDo: actionBlock
	self do: [:node | (predicateBlock value: node) 
		ifTrue: [actionBlock value: node]]