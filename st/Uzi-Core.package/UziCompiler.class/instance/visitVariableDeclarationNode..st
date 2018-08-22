visiting
visitVariableDeclarationNode: node
	| varName |
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
	varName := self registerNameFor: node.
	node value 
		ifNil: [script registerLocal: varName]
		ifNotNil: [:value |
			value isNumberLiteral
				ifTrue: [script registerLocal: varName value: value value]
				ifFalse: [script registerLocal: varName.
					self visit: value.
					(script pop: varName) node: node]]