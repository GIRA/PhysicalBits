private
resolveImport: import
	| ast |
	(imports anySatisfy: [:each | each alias = import alias and: [each path = import path]])
		ifTrue: [self error: 'Dependency cycle detected' on: import].
		
	(librariesDirectory fileExists: import path)
		ifFalse: [self error: 'File not found' on: import].
	
	ast := self parseFile: import path.
		
	import initializationBlock ifNotNil: [:initBlock | 
		ast := self applyInitializationBlock: initBlock on: ast].
	
	imports addLast: import.
	[ast := self applyOn: ast implicitImports: (self implicitImportsFor: import).
	self flag: #Richo.
	"TODO(Richo): Here we should check for errors in the ast, right after we
	resolved the imports.
	Uzi check: ast."]
		ensure: [imports removeLast].
		
	import alias
		ifNotNil: [:alias | ast := self applyAlias: alias , '.' on: ast].
	
	^ ast