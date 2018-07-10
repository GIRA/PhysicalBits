connection
openPort: portString
	UziTranscript cr; log: 'Opening port: {1}' format: { portString }.
	serial := serial baudRate: 57600; openPort: portString.
	serial ifNil: [UziTranscript log: 'Opening port failed!']