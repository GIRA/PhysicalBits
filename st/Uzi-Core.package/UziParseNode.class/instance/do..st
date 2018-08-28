enumerating
do: aBlock
	"Pre-order traversal"
	aBlock value: self.
	self children 
		select: [:each | each notNil]
		thenDo: [:each | each do: aBlock]