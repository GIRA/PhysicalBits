instance creation
readFrom: debugger stack: stack pc: pc fp: fp
	"Recursively interprets the stack and returns an array of stack frames, ordered
	from top to bottom"
	| frame rest script val |
	stack ifNil: [^ #()].
	stack ifEmpty: [^ #()].
	(script := debugger scriptForPC: pc) ifNil: [^ #()].
		
	^ Array streamContents: [:stream |
		frame := UziStackFrame
			script: script
			pc: pc
			fp: fp
			stack: stack.
		stream nextPut: frame.
		val := stack basicAt: 2 + fp + script variables size.
		rest := (self 
			readFrom: debugger
			stack: (stack copyFrom: 1 to: fp)
			pc: (val bitAnd: 16rFFFF)
			fp: ((val >> 16) bitAnd: 16rFFFF)).
		stream nextPutAll: rest]