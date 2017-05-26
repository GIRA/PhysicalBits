tests
testImportsList
	| pp imports |
	pp := parser importsList.
	imports := pp parse: '
import M1 = ''DCMotor.uzi''
import M2 = ''DCMotor.uzi''

	import mutex = ''Mutex.uzi''
'.
	self assert: 3 equals: imports size.
	self assert: 'M1' equals: imports first name.
	self assert: 'M2' equals: imports second name.
	self assert: 'mutex' equals: imports third name.
	self assert: 'DCMotor.uzi' equals: imports first path.
	self assert: 'DCMotor.uzi' equals: imports second path.
	self assert: 'Mutex.uzi' equals: imports third path