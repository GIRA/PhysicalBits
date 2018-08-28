testing
isExpression 
	"Script calls are always expressions"
	self isPrimitive ifFalse: [^ true].
	
	"When calling a primitive, it depends on the primitive"
	^ self primitive
		ifNil: true
		ifNotNil: [:prim | prim isExpression]