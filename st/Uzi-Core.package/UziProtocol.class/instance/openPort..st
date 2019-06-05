connection
openPort: portString
	UziOutput log: 'Opening port: {1}' format: { portString }; flush.
	serial := serial baudRate: 57600; openPort: portString.
	serial ifNil: [UziOutput log: 'Opening port failed!'; flush]