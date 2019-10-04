server
openBrowser
	| commands os url |
	url := 'http://localhost:8080/'.
	commands := Dictionary new
		at: 'Win32' put: 'explorer "{1}"';
		at: 'Mac OS' put: 'open {1}';
		at: 'unix' put: 'xdg-open {1}';
		yourself.
	os := Smalltalk 	at: #OSProcess ifAbsent: [nil].
	os ifNil: ["Show a message in the transcript to remember to install OSProcess"
		Transcript cr; show: 'OSProcess is needed to open the browser automatically'; cr.
		^ self].
	commands at: Smalltalk platformName
		ifPresent: [:cmd | os perform: #command: with: (cmd format: {url})]