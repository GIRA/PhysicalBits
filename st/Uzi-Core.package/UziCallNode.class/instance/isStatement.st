testing
isStatement 
	"Script calls are always expressions but the compiler 
	can treat them as statements"
	self isPrimitive ifFalse: [^ true].
	
	"When calling a primitive, it depends on the primitive"
	^ self primitive
		ifNil: true
		ifNotNil: [:prim | prim isStatement]