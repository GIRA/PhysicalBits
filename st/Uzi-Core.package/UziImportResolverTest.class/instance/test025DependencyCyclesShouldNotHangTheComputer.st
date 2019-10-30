tests
test025DependencyCyclesShouldNotHangTheComputer
	"
	import a = 'test15.uzi';
	import b = 'test16.uzi';
	"
	| original |
	original := program 
		imports: {
			import alias: #a path: 'test15.uzi'.
			import alias: #b path: 'test16.uzi' 
		}
		globals: #()
		scripts: {}
		primitives: primitives.
	self should: [ir applyOn: original] raise: UziImportError