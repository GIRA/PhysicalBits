accessing
at: key do: aBlock
	self at: key do: aBlock ifTrue: [true]