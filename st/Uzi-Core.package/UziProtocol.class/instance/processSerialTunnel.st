receiving
processSerialTunnel 
	| value |
	value := serial next.
	UziOutput log: 'SERIAL: {1}' format: { value }; flush