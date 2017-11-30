visiting
visitVariableDeclarationNode: node
	script ifNil: ["Globals can only be initialized with numbers (no expressions).
		If no value is defined we use 0 as default value"
		program registerGlobal: node name 
			value: (node value ifNil: [0] ifNotNil: [:v | v value]).
		^ self].
	"Locals are different. If they have no explicit value we use 0 as default.
	If the value is a number, we simply use it as initial value. If the value is an 
	expression, we register the local with default value and then we compile an
	assignment that evaluates the expression."
	self flag: #Richo. "Early optimization!"
	node value 
		ifNil: [script registerLocal: node name]
		ifNotNil: [:value |
			value isNumber
				ifTrue: [script registerLocal: node name value: value value.
					"This optimization doesn't currently work because local variables are not
					encoded with their initial value yet. So I'm also going through the slow path 
					of compiling the assignment after the declaration" 
					self flag: #Richo.
					self visit: value.
					(script pop: node name) node: node]
				ifFalse: [script registerLocal: node name.
					self visit: value.
					(script pop: node name) node: node]]