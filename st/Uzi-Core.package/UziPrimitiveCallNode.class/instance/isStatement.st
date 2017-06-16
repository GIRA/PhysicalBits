testing
isStatement 
	^ primitive 
		ifNil: true
		ifNotNil: [:prim | prim isStatement ]