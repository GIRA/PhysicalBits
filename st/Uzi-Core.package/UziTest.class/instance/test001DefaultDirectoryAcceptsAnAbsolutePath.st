tests
test001DefaultDirectoryAcceptsAnAbsolutePath
	| dir |
	dir := FileDirectory on: 'C:\A\B\C'.
	Uzi defaultDirectory: dir.
	self assert: dir equals: Uzi defaultDirectory.
	self assert: #('C:' 'A' 'B' 'C') equals: Uzi defaultPath