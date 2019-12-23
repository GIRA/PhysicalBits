connection
openPort: portString
	Uzi logger log: 'Opening port: %1' format: { portString }; flush.
	serial := serial baudRate: 57600; openPort: portString.
	serial ifNil: [Uzi logger error: 'Opening port failed!'; flush]