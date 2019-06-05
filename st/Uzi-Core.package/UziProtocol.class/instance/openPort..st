connection
openPort: portString
	Uzi out log: 'Opening port: {1}' format: { portString }; flush.
	serial := serial baudRate: 57600; openPort: portString.
	serial ifNil: [Uzi out log: 'Opening port failed!'; flush]