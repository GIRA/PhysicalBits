connection
openPort: portString
	UziTranscript log: 'Opening port: {1}' format: { portString }.
	serial := serial baudRate: 57600; openPort: portString.
	serial ifNil: [UziTranscript log: 'Opening port failed!']