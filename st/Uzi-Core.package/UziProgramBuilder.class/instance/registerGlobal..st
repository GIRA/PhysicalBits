private
registerGlobal: arg
	"If the argument is a string we interpret it as the variable name, otherwise
	we treat it as value."
	^ arg isString 
		ifTrue: [variables detect: [:v | v name = arg] 
			ifNone: [variables add: (UziVariable named: arg)]]
		ifFalse: [variables detect: [:v | v value = arg and: [v isConstant]]
			ifNone: [variables add: (UziVariable value: arg)]]