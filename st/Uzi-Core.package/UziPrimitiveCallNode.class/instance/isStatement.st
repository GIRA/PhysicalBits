testing
isStatement 
	^ self primitive 
		ifNil: true
		ifNotNil: [:prim | prim isStatement ]