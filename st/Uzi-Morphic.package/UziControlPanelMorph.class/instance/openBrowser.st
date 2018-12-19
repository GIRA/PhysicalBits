server
openBrowser
	Smalltalk platformName ~= 'Win32' ifTrue: [^ self].
	Smalltalk at: #OSProcess
		ifPresent: [:os | os perform: #command: with: 'explorer "http://localhost:8080/"']