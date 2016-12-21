accessing
resourceFor: anObject
	children do: [:each | (each resourceFor: anObject)
		ifNotNil: [:resource | ^ resource]].
	^ nil