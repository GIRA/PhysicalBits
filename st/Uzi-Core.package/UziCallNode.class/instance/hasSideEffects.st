testing
hasSideEffects
	"Script calls could always have side effects"
	self isPrimitive ifFalse: [^ true].

	"If one of my arguments has side effects, then I also have side effects"
	(self arguments anySatisfy: [:arg | arg value hasSideEffects])
		ifTrue: [^ true].
		
	"When calling a primitive, it depends on the primitive"
	^ self primitive
		ifNil: true
		ifNotNil: [:prim | prim isExpression not]