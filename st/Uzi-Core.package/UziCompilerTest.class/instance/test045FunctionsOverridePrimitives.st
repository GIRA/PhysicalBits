tests
test045FunctionsOverridePrimitives
	| program main millis |
	program := self compile: '
	task main() running { delayMs(millis()); }
	func millis() { return -1; }'.
	main := program scripts detect: [:s | s name = #main].
	millis := program scripts detect: [:s | s name = #millis].
	self assert: main instructions = {
		call arg: #millis.
		prim arg: (UziPrimitive named: #delayMs)
	}.
	self assert: millis instructions = {
		push arg: (program constants detect: [:each | each value = -1]).
		prim arg: (UziPrimitive named: #retv)
	}
