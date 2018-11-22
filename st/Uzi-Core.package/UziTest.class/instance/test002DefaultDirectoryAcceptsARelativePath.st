tests
test002DefaultDirectoryAcceptsARelativePath
	| dir |
	dir := FileDirectory default.
	Uzi defaultDirectory: dir.
	self assert: dir equals: Uzi defaultDirectory.
	self assert: #() equals: Uzi defaultPath