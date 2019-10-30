private
resolveImport: import
	| ast |
	(imports anySatisfy: [:each | each alias = import alias and: [each path = import path]])
		ifTrue: [self error: 'Dependency cycle detected'].
		
	(librariesDirectory fileExists: import path)
		ifFalse: [self error: 'File not found'].
	
	ast := librariesDirectory 
		readOnlyFileNamed: import path
		do: [:file | parser parse: file contents].
	
	import initializationBlock ifNotNil: [:initBlock | 
		ast := self applyInitializationBlock: initBlock on: ast].
	
	imports addLast: import.
	[ast := self applyOn: ast implicitImports: (self implicitImportsFor: import)]
		ensure: [imports removeLast].
		
	(ast isPetitFailure not and: [import alias notNil])
		ifTrue: [ast := self applyAlias: import alias , '.' on: ast].
	
	^ ast