testing
hasSideEffects
	"Script calls could always have side effects"
	self isPrimitive ifFalse: [^ true].
	
	"When calling a primitive, it depends on the primitive"
	^ self primitive
		ifNil: true
		ifNotNil: [:prim | prim isExpression not]