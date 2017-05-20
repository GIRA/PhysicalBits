button actions
openDebugger
	self isConnected ifFalse: [^ self].
	self tickingScripts ifNotEmpty: [:options |
		(UIManager default
			chooseFrom: (options collect: [:each | each name printString])
			values: options
			title: 'Choose script')
			ifNotNil: [:selection | self openDebuggerOn: selection]]