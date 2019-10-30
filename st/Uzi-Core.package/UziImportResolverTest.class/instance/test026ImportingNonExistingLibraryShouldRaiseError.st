tests
test026ImportingNonExistingLibraryShouldRaiseError
	"
	import a = 'test0_NO_EXISTE.uzi';
	"
	| original |
	original := program 
		imports: {
			import alias: #a path: 'test0_NO_EXISTE.uzi'.
		}
		globals: #()
		scripts: {}
		primitives: primitives.
	self should: [ir applyOn: original] raise: UziImportError