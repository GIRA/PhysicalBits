tests
test001DefaultDirectoryAcceptsAnAbsolutePath
	| dir expected isWindows |
	isWindows := Smalltalk platformName = 'Win32'.
	dir := FileDirectory on: (isWindows ifTrue: 'C:\A\B\C' ifFalse: '/A/B/C').
	expected := isWindows ifTrue: #('C:' 'A' 'B' 'C') ifFalse: #('A' 'B' 'C').
	Uzi defaultDirectory: dir.
	self assert: dir equals: Uzi defaultDirectory.
	self assert: expected equals: Uzi defaultPath