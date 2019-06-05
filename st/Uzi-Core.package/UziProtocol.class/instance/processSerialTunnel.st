receiving
processSerialTunnel 
	| value |
	value := serial next.
	Uzi out log: 'SERIAL: {1}' format: { value }; flush